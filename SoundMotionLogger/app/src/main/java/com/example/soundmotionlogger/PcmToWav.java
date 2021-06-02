package com.example.soundmotionlogger;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.example.soundmotionlogger.WaveHeader;

public class PcmToWav {

    public static void convertAudioFiles(String pcmPath, String wavPath, int samplingRate) throws Exception {

        FileInputStream fis = new FileInputStream(pcmPath);
        // Calculate the length
        byte[] buf = new byte[1024 * 4];
        int size = fis.read(buf);
        int PCMSize = 0;
        while (size != -1) {
            PCMSize += size;
            size = fis.read(buf);
        }
        fis.close();

        // Fill in the parameters, bit rate and so on. Here is the 16-bit mono 16000 hz
        WaveHeader header = new WaveHeader();
        //length field = size of the content (PCMSize) + size of the header field (excluding the first 4 bytes of the identifier RIFF and the fileLength itself 4 bytes)
        header.fileLength = PCMSize + (44 - 8);
        header.FmtHdrLeth = 16;
        header.BitsPerSample = 16;
        header.Channels = 1;
        header.FormatTag = 0x0001;
        header.SamplesPerSec = samplingRate;
        header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
        header.DataHdrLeth = PCMSize;

        byte[] h = header.getHeader();

        if (BuildConfig.DEBUG && h.length != 44) {
            throw new AssertionError("Assertion failed");
        }

//		auline.write(h, 0, h.length);

        byte[] b = new byte[10];

        FileOutputStream fs = new FileOutputStream(wavPath);
        fs.write(h);
        FileInputStream fiss = new FileInputStream(pcmPath);
        byte[] bb = new byte[10];
        int len = -1;
        while ((len = fiss.read(bb)) > 0) {
            fs.write(bb, 0, len);
        }

        fs.close();
        fiss.close();
    }
}


/**
 * WavHeader helper class. Used to generate header information.
 * @author Administrator
 *
 */
class WaveHeader {
    public final char fileID[] = {'R', 'I', 'F', 'F'};
    public int fileLength;
    public short FormatTag;
    public short Channels;
    public int SamplesPerSec;
    public int AvgBytesPerSec;
    public short BlockAlign;
    public short BitsPerSample;
    public char DataHdrID[] = {'d','a','t','a'};
    public int DataHdrLeth;
    public char wavTag[] = {'W', 'A', 'V', 'E'};;
    public char FmtHdrID[] = {'f', 'm', 't', ' '};
    public int FmtHdrLeth;

    public WaveHeader() {}//no-parameter constructor

    public byte[] getHeader() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        WriteChar(bos, fileID);
        WriteInt(bos, fileLength);
        WriteChar(bos, wavTag);
        WriteChar(bos, FmtHdrID);
        WriteInt(bos,FmtHdrLeth);
        WriteShort(bos,FormatTag);
        WriteShort(bos,Channels);
        WriteInt(bos,SamplesPerSec);
        WriteInt(bos,AvgBytesPerSec);
        WriteShort(bos,BlockAlign);
        WriteShort(bos,BitsPerSample);
        WriteChar(bos,DataHdrID);
        WriteInt(bos,DataHdrLeth);
        bos.flush();
        byte[] r = bos.toByteArray();
        bos.close();
        return r;
    }

    private void WriteShort(ByteArrayOutputStream bos, int s) throws IOException {
        byte[] mybyte = new byte[2];
        mybyte[1] =(byte)( (s << 16) >> 24 );
        mybyte[0] =(byte)( (s << 24) >> 24 );
        bos.write(mybyte);
    }


    private void WriteInt(ByteArrayOutputStream bos, int n) throws IOException {
        byte[] buf = new byte[4];
        buf[3] =(byte)( n >> 24 );
        buf[2] =(byte)( (n << 8) >> 24 );
        buf[1] =(byte)( (n << 16) >> 24 );
        buf[0] =(byte)( (n << 24) >> 24 );
        bos.write(buf);
    }

    private void WriteChar(ByteArrayOutputStream bos, char[] id) {
        for (int i=0; i<id.length; i++) {
            char c = id[i];
            bos.write(c);
        }
    }
}
