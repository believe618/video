package com.googlecode.mp4parser.authoring.tracks;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.coremedia.iso.boxes.AbstractMediaHeaderBox2;
import com.coremedia.iso.boxes.CompositionTimeToSample2;
import com.coremedia.iso.boxes.SampleDependencyTypeBox2;
import com.coremedia.iso.boxes.SampleDescriptionBox2;
import com.coremedia.iso.boxes.SoundMediaHeaderBox2;
import com.coremedia.iso.boxes.SubSampleInformationBox2;
import com.coremedia.iso.boxes.TimeToSampleBox2;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry22;
import com.googlecode.mp4parser.authoring.AbstractTrack22;
import com.googlecode.mp4parser.authoring.TrackMetaData2;
import com.googlecode.mp4parser.boxes.mp4.ESDescriptorBox2;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.AudioSpecificConfig2;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer2;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.DecoderConfigDescriptor2;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.ESDescriptor2;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.SLConfigDescriptor2;

/**
 * Created with IntelliJ IDEA.
 * User: magnus
 * Date: 2012-04-20
 * Time: 11:14
 * To change this template use File | Settings | File Templates.
 */
public class AACTrack22Impl2 extends AbstractTrack22 {
    TrackMetaData2 trackMetaData2 = new TrackMetaData2();
    SampleDescriptionBox2 sampleDescriptionBox;

    int samplerate;
    int bitrate;
    int channelCount;
    int channelconfig;

    int bufferSizeDB;
    long maxBitRate;
    long avgBitRate;

    private PushbackInputStream inputStream;
    private List<ByteBuffer> samples;
    boolean readSamples = false;
    List<TimeToSampleBox2.Entry> stts;
    public static Map<Integer, Integer> samplingFrequencyIndexMap = new HashMap<Integer, Integer>();

    public AACTrack22Impl2(PushbackInputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        stts = new LinkedList<TimeToSampleBox2.Entry>();

        samplingFrequencyIndexMap.put(96000, 0);
        samplingFrequencyIndexMap.put(88200, 1);
        samplingFrequencyIndexMap.put(64000, 2);
        samplingFrequencyIndexMap.put(48000, 3);
        samplingFrequencyIndexMap.put(44100, 4);
        samplingFrequencyIndexMap.put(32000, 5);
        samplingFrequencyIndexMap.put(24000, 6);
        samplingFrequencyIndexMap.put(22050, 7);
        samplingFrequencyIndexMap.put(16000, 8);
        samplingFrequencyIndexMap.put(12000, 9);
        samplingFrequencyIndexMap.put(11025, 10);
        samplingFrequencyIndexMap.put(8000, 11);
        samplingFrequencyIndexMap.put(0x0, 96000);
        samplingFrequencyIndexMap.put(0x1, 88200);
        samplingFrequencyIndexMap.put(0x2, 64000);
        samplingFrequencyIndexMap.put(0x3, 48000);
        samplingFrequencyIndexMap.put(0x4, 44100);
        samplingFrequencyIndexMap.put(0x5, 32000);
        samplingFrequencyIndexMap.put(0x6, 24000);
        samplingFrequencyIndexMap.put(0x7, 22050);
        samplingFrequencyIndexMap.put(0x8, 16000);
        samplingFrequencyIndexMap.put(0x9, 12000);
        samplingFrequencyIndexMap.put(0xa, 11025);
        samplingFrequencyIndexMap.put(0xb, 8000);

        if (!readVariables()) {
            throw new IOException();
        }

        samples = new LinkedList<ByteBuffer>();
        if (!readSamples()) {
            throw new IOException();
        }

        double packetsPerSecond = (double)samplerate / 1024.0;
        double duration = samples.size() / packetsPerSecond;

        long dataSize = 0;
        LinkedList<Integer> queue = new LinkedList<Integer>();
        for (int i = 0; i < samples.size(); i++) {
            int size = samples.get(i).capacity();
            dataSize += size;
            queue.add(size);
            while (queue.size() > packetsPerSecond) {
                queue.removeFirst();
            }
            if (queue.size() == (int) packetsPerSecond) {
                int currSize = 0;
                for (int j = 0 ; j < queue.size(); j++) {
                    currSize += queue.get(j);
                }
                double currBitrate = 8.0 * currSize / queue.size() * packetsPerSecond;
                if (currBitrate > maxBitRate) {
                    maxBitRate = (int)currBitrate;
                }
            }
        }

        avgBitRate = (int) (8 * dataSize / duration);

        bufferSizeDB = 1536; /* TODO: Calcultate this somehow! */

        sampleDescriptionBox = new SampleDescriptionBox2();
        AudioSampleEntry22 audioSampleEntry2 = new AudioSampleEntry22("mp4a");
        audioSampleEntry2.setChannelCount(2);
        audioSampleEntry2.setSampleRate(samplerate);
        audioSampleEntry2.setDataReferenceIndex(1);
        audioSampleEntry2.setSampleSize(16);


        ESDescriptorBox2 esds = new ESDescriptorBox2();
        ESDescriptor2 descriptor = new ESDescriptor2();
        descriptor.setEsId(0);

        SLConfigDescriptor2 slConfigDescriptor = new SLConfigDescriptor2();
        slConfigDescriptor.setPredefined(2);
        descriptor.setSlConfigDescriptor(slConfigDescriptor);

        DecoderConfigDescriptor2 decoderConfigDescriptor = new DecoderConfigDescriptor2();
        decoderConfigDescriptor.setObjectTypeIndication(0x40);
        decoderConfigDescriptor.setStreamType(5);
        decoderConfigDescriptor.setBufferSizeDB(bufferSizeDB);
        decoderConfigDescriptor.setMaxBitRate(maxBitRate);
        decoderConfigDescriptor.setAvgBitRate(avgBitRate);

        AudioSpecificConfig2 audioSpecificConfig2 = new AudioSpecificConfig2();
        audioSpecificConfig2.setAudioObjectType(2); // AAC LC
        audioSpecificConfig2.setSamplingFrequencyIndex(samplingFrequencyIndexMap.get(samplerate));
        audioSpecificConfig2.setChannelConfiguration(channelconfig);
        decoderConfigDescriptor.setAudioSpecificInfo(audioSpecificConfig2);

        descriptor.setDecoderConfigDescriptor(decoderConfigDescriptor);

        ByteBuffer data = descriptor.serialize();
        esds.setData(data);
        audioSampleEntry2.addBox(esds);
        sampleDescriptionBox.addBox(audioSampleEntry2);

        trackMetaData2.setCreationTime(new Date());
        trackMetaData2.setModificationTime(new Date());
        trackMetaData2.setLanguage("eng");
        trackMetaData2.setTimescale(samplerate); // Audio tracks always use samplerate as timescale

     }

    public SampleDescriptionBox2 getSampleDescriptionBox() {
        return sampleDescriptionBox;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<TimeToSampleBox2.Entry> getDecodingTimeEntries() {
        return stts;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<CompositionTimeToSample2.Entry> getCompositionTimeEntries() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long[] getSyncSamples() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<SampleDependencyTypeBox2.Entry> getSampleDependencies() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public TrackMetaData2 getTrackMetaData2() {
        return trackMetaData2;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHandler() {
        return "soun";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<ByteBuffer> getSamples() {
        return samples;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AbstractMediaHeaderBox2 getMediaHeaderBox() {
        return new SoundMediaHeaderBox2();
    }

    public SubSampleInformationBox2 getSubsampleInformationBox() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private boolean readVariables() throws IOException {
        byte[] data = new byte[100];
        if (100 != inputStream.read(data, 0, 100)) {
            return false;
        }
        inputStream.unread(data);

        ByteBuffer bb = ByteBuffer.wrap(data);
        BitReaderBuffer2 brb = new BitReaderBuffer2(bb);
        int syncword = brb.readBits(12);

        if (syncword != 0xfff) {
            return false;
        }
        int id = brb.readBits(1);
        int layer = brb.readBits(2);
        int protectionAbsent = brb.readBits(1);
        int profile = brb.readBits(2);
        samplerate = samplingFrequencyIndexMap.get(brb.readBits(4));
        brb.readBits(1);
        channelconfig = brb.readBits(3);
        int original = brb.readBits(1);
        int home = brb.readBits(1);
        int emphasis = brb.readBits(2);

        return true;
    }

    private boolean readSamples() throws IOException {
        if (readSamples) {
            return true;
        }

        readSamples = true;
        byte[] header = new byte[15];
        boolean ret = false;
        while (-1 != inputStream.read(header)) {
            ret = true;
            ByteBuffer bb = ByteBuffer.wrap(header);

            inputStream.unread(header);
            BitReaderBuffer2 brb = new BitReaderBuffer2(bb);
            int syncword = brb.readBits(12);

            if (syncword != 0xfff) {
                return false;
            }
            brb.readBits(3);
            int protectionAbsent = brb.readBits(1);
            brb.readBits(14);
            int frameSize = brb.readBits(13);
            int bufferFullness = brb.readBits(11);
            int noBlocks = brb.readBits(2);
            int used = (int) Math.ceil(brb.getPosition() / 8.0);
            if (protectionAbsent == 0) {
                used += 2;
            }
            inputStream.skip(used);
            frameSize -= used;
//            System.out.println("Size: " + frameSize + " fullness: " + bufferFullness + " no blocks: " + noBlocks);
            byte[] data = new byte[frameSize];
            inputStream.read(data);
            samples.add(ByteBuffer.wrap(data));
            stts.add(new TimeToSampleBox2.Entry(1, 1024));

        }
        return ret;
    }
}

