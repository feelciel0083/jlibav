/*
 * Copyright (C) 2012 Ondrej Perutka
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package org.libav.avcodec;

import org.bridj.Pointer;
import org.libav.LibavException;
import org.libav.avcodec.bridge.AVCodecContext53;
import org.libav.avcodec.bridge.AVCodecLibrary;
import org.libav.avutil.MediaType;
import org.libav.avutil.PixelFormat;
import org.libav.avutil.SampleFormat;
import org.libav.avutil.bridge.AVUtilLibrary;
import org.libav.bridge.LibraryManager;
import org.libav.util.Rational;

/**
 * Wrapper class for the AVCodecContext53.
 * 
 * @author Ondrej Perutka
 */
public class CodecContextWrapper53 extends AbstractCodecContextWrapper {
    
    private static final int DEFAULT_OUTPUT_BUFFER_SIZE = 256 * 1024;
    
    private static final AVCodecLibrary codecLib;
    private static final AVUtilLibrary utilLib;
    
    private static final boolean avcOpen2;
    private static final boolean avcDecodeAudio4;
    private static final boolean avcEncodeAudio2;
    
    static {
        LibraryManager lm = LibraryManager.getInstance();
        codecLib = lm.getAVCodecLibrary();
        utilLib = lm.getAVUtilLibrary();
        
        avcOpen2 = codecLib.functionExists("avcodec_open2");
        avcDecodeAudio4 = codecLib.functionExists("avcodec_decode_audio4");
        avcEncodeAudio2 = codecLib.functionExists("avcodec_encode_audio2");
    }
    
    private AVCodecContext53 context;
    private boolean closed;
    
    private Pointer<Integer> intByRef;
    private IDecodeAudioFunction decodeAudioFunction;
    private IEncodeAudioFunction encodeAudioFunction;
    
    private int outputBufferSize;
    private Pointer<Byte> outputBuffer;
    
    /**
     * Create a new wrapper for the given AVCodecContext.
     * 
     * @param context a codec context structure
     */
    public CodecContextWrapper53(AVCodecContext53 context) {
        this.context = context;
        this.closed = true;
        
        this.intByRef = Pointer.allocateInt();
        
        if (avcDecodeAudio4)
            this.decodeAudioFunction = new DecodeAudio4();
        else
            this.decodeAudioFunction = new DecodeAudio3();
        
        if (avcEncodeAudio2)
            this.encodeAudioFunction = new EncodeAudio2();
        else
            this.encodeAudioFunction = new EncodeAudio();
        
        outputBufferSize = DEFAULT_OUTPUT_BUFFER_SIZE;
        outputBuffer = null;
    }

    @Override
    public void clearWrapperCache() {
        super.clearWrapperCache();
        
        rebindCodedFrame();
    }
    
    private void rebindCodedFrame() {
        if (context == null || codedFrame == null)
            return;
        
        Pointer<?> ptr = context.coded_frame();
        if (ptr == null)
            codedFrame = null;
        else
            codedFrame.rebind(ptr);
    }
    
    @Override
    public Pointer<?> getPointer() {
        if (context == null)
            return null;
        
        return Pointer.pointerTo(context);
    }

    @Override
    public void rebind(Pointer<?> pointer) {
        context = new AVCodecContext53(pointer);
    }

    @Override
    public void getDefaults(ICodecWrapper codec) throws LibavException {
        int result = codecLib.avcodec_get_context_defaults3(getPointer(), codec.getPointer());
        if (result != 0)
            throw new LibavException(result);
        
        clearWrapperCache();
    }
    
    @Override
    public void open(ICodecWrapper codec) throws LibavException {
        if (!isClosed())
            return;
        
        int result;
        if (avcOpen2)
            result = codecLib.avcodec_open2(getPointer(), codec.getPointer(), null);
        else
            result = codecLib.avcodec_open(getPointer(), codec.getPointer());
        
        if(result < 0)
            throw new LibavException(result);
        
        closed = false;
    }
    
    @Override
    public void close() {
        if (isClosed())
            return;
        
        codecLib.avcodec_close(getPointer());
        closed = true;
    }
    
    @Override
    public void free() {
        close();
        
        if (context != null)
            utilLib.av_free(getPointer());
        if (outputBuffer != null)
            utilLib.av_free(outputBuffer);
        
        context = null;
        outputBuffer = null;
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public IFrameWrapper getCodedFrame() {
        if (context == null)
            return null;
        
        if (codedFrame == null) {
            Pointer<?> p = context.coded_frame();
            codedFrame = p == null ? null : FrameWrapperFactory.getInstance().wrap(p);
        }
        
        return codedFrame;
    }
    
    @Override
    public MediaType getCodecType() {
        if (context == null)
            return null;
        
        if (codecType == null)
            codecType = MediaType.valueOf(context.codec_type());
        
        return codecType;
    }
    
    @Override
    public void setCodecType(MediaType codecType) {
        if (context == null)
            return;
        
        context.codec_type(codecType.value());
        this.codecType = codecType;
    }
    
    @Override
    public CodecID getCodecId() {
        if (context == null)
            return null;
        
        if (codecId == null)
            codecId = CodecID.valueOf(context.codec_id());
        
        return codecId;
    }
    
    @Override
    public void setCodecId(CodecID codecId) {
        if (context == null)
            return;
        
        context.codec_id(codecId.value());
        this.codecId = codecId;
    }
    
    @Override
    public int getFlags() {
        if (context == null)
            return 0;
        
        if (flags == null)
            flags = context.flags();
        
        return flags;
    }
    
    @Override
    public void setFlags(int flags) {
        if (context == null)
            return;
        
        context.flags(flags);
        this.flags = flags;
    }
    
    @Override
    public int getWidth() {
        if (context == null)
            return 0;
        
        if (width == null)
            width = context.width();
        
        return width;
    }
    
    @Override
    public void setWidth(int width) {
        if (context == null)
            return;
        
        context.width(width);
        this.width = width;
    }
    
    @Override
    public int getHeight() {
        if (context == null)
            return 0;
        
        if (height == null)
            height = context.height();
        
        return height;
    }
    
    @Override
    public void setHeight(int height) {
        if (context == null)
            return;
        
        context.height(height);
        this.height = height;
    }

    @Override
    public Rational getSampleAspectRatio() {
        if (context == null)
            return null;
        
        if (sampleAspectRatio == null)
            sampleAspectRatio = new Rational(context.sample_aspect_ratio());
        
        return sampleAspectRatio;
    }

    @Override
    public void setSampleAspectRatio(Rational sampleAspectRatio) {
        if (context == null)
            return;
        
        if (sampleAspectRatio == null)
            sampleAspectRatio = new Rational(0, 0);
        
        context.sample_aspect_ratio().num((int)sampleAspectRatio.getNumerator());
        context.sample_aspect_ratio().den((int)sampleAspectRatio.getDenominator());
        this.sampleAspectRatio = sampleAspectRatio;
    }

    @Override
    public int getChromaSampleLocation() {
        if (context == null)
            return 0;
        
        if (chromaSampleLocation == null)
            chromaSampleLocation = context.chroma_sample_location();
        
        return chromaSampleLocation;
    }

    @Override
    public void setChromaSampleLocation(int chromaSampleLocation) {
        if (context == null)
            return;
        
        context.chroma_sample_location(chromaSampleLocation);
        this.chromaSampleLocation = chromaSampleLocation;
    }
    
    @Override
    public PixelFormat getPixelFormat() {
        if (context == null)
            return null;
        
        if (pixelFormat == null)
            pixelFormat = PixelFormat.valueOf(context.pix_fmt());
        
        return pixelFormat;
    }
    
    @Override
    public void setPixelFormat(PixelFormat pixelFormat) {
        if (context == null)
            return;
        
        context.pix_fmt(pixelFormat.value());
        this.pixelFormat = pixelFormat;
    }

    @Override
    public int getBitRate() {
        if (context == null)
            return 0;
        
        if (bitRate == null)
            bitRate = context.bit_rate();
        
        return bitRate;
    }

    @Override
    public void setBitRate(int bitRate) {
        if (context == null)
            return;
        
        context.bit_rate(bitRate);
        this.bitRate = bitRate;
    }

    @Override
    public Rational getTimeBase() {
        if (context == null)
            return null;
        
        if (timeBase == null)
            timeBase = new Rational(context.time_base());
        
        return timeBase;
    }

    @Override
    public void setTimeBase(Rational timeBase) {
        if (context == null)
            return;
        
        if (timeBase == null)
            timeBase = new Rational(0, 0);
        
        context.time_base().num((int)timeBase.getNumerator());
        context.time_base().den((int)timeBase.getDenominator());
        this.timeBase = timeBase;
    }

    @Override
    public int getGopSize() {
        if (context == null)
            return 0;
        
        if (gopSize == null)
            gopSize = context.gop_size();
        
        return gopSize;
    }

    @Override
    public void setGopSize(int gopSize) {
        if (context == null)
            return;
        
        context.gop_size(gopSize);
        this.gopSize = gopSize;
    }

    @Override
    public int getMaxBFrames() {
        if (context == null)
            return 0;
        
        if (maxBFrames == null)
            maxBFrames = context.max_b_frames();
        
        return maxBFrames;
    }

    @Override
    public void setMaxBFrames(int maxBFrames) {
        if (context == null)
            return;
        
        context.max_b_frames(maxBFrames);
        this.maxBFrames = maxBFrames;
    }

    @Override
    public int getMbDecision() {
        if (context == null)
            return 0;
        
        if (mbDecision == null)
            mbDecision = context.mb_decision();
        
        return mbDecision;
    }

    @Override
    public void setMbDecision(int mbDecision) {
        if (context == null)
            return;
        
        context.mb_decision(mbDecision);
        this.mbDecision = mbDecision;
    }

    @Override
    public int getChannels() {
        if (context == null)
            return 0;
        
        if (channels == null)
            channels = context.channels();
        
        return channels;
    }

    @Override
    public void setChannels(int channels) {
        if (context == null)
            return;
        
        context.channels(channels);
        this.channels = channels;
    }

    @Override
    public long getChannelLayout() {
        if (context == null)
            return 0;
        
        if (channelLayout == null)
            channelLayout = context.channel_layout();
        
        return channelLayout;
    }

    @Override
    public void setChannelLayout(long channelLayout) {
        if (context == null)
            return;
        
        context.channel_layout(channelLayout);
        this.channelLayout = channelLayout;
    }

    @Override
    public SampleFormat getSampleFormat() {
        if (context == null)
            return null;
        
        if (sampleFormat == null)
            sampleFormat = SampleFormat.valueOf(context.sample_fmt());
        
        return sampleFormat;
    }

    @Override
    public void setSampleFormat(SampleFormat sampleFormat) {
        if (context == null)
            return;
        
        context.sample_fmt(sampleFormat.value());
        this.sampleFormat = sampleFormat;
    }

    @Override
    public int getSampleRate() {
        if (context == null)
            return 0;
        
        if (sampleRate == null)
            sampleRate = context.sample_rate();
        
        return sampleRate;
    }

    @Override
    public void setSampleRate(int sampleRate) {
        if (context == null)
            return;
        
        context.sample_rate(sampleRate);
        this.sampleRate = sampleRate;
    }
    
    @Override
    public int getFrameSize() {
        if (context == null)
            return 0;
        
        if (frameSize == null)
            frameSize = context.frame_size();
        
        return frameSize;
    }
    
    @Override
    public boolean decodeVideoFrame(IPacketWrapper packet, IFrameWrapper frame) throws LibavException {
        if (isClosed())
            return false;
        
        intByRef.setInt(0);
        
        int packetSize = packet.getSize();
        int len = codecLib.avcodec_decode_video2(getPointer(), frame.getPointer(), intByRef, packet.getPointer());
        if (len < 0)
            throw new LibavException(len);
        
        packetSize -= len;
        packet.setSize(packetSize);
        packet.setData(packetSize <= 0 ? null : packet.getData().offset(len));
        if (intByRef.getInt() != 0) {
            frame.clearWrapperCache();
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean encodeVideoFrame(IFrameWrapper frame, IPacketWrapper packet) throws LibavException {
        if (isClosed())
            return false;
        
        if (outputBuffer == null)
            outputBuffer = utilLib.av_malloc(outputBufferSize).as(Byte.class);
        if (outputBuffer == null)
            throw new OutOfMemoryError("not enough memory for the video frame encoder");
        
        packet.init();
        packet.setData(outputBuffer);
        packet.setSize(outputBufferSize);
        
        int size = codecLib.avcodec_encode_video(getPointer(), outputBuffer, outputBufferSize, frame == null ? null : frame.getPointer());
        if (size < 0)
            throw new LibavException(size);
        else if (size == 0)
            return false;
        
        rebindCodedFrame();
        IFrameWrapper cf = getCodedFrame();
        cf.clearWrapperCache();
        
        packet.setPts(cf.getPts());
        packet.setSize(size);
        
        return true;
    }
    
    @Override
    public boolean decodeAudioFrame(IPacketWrapper packet, IFrameWrapper frame) throws LibavException {
        if (isClosed())
            return false;
        
        return decodeAudioFunction.decodeAudioFrame(packet, frame);
    }
    
    @Override
    public boolean encodeAudioFrame(IFrameWrapper frame, IPacketWrapper packet) throws LibavException {
        if (isClosed())
            return false;
        
        return encodeAudioFunction.encodeAudioFrame(frame, packet);
    }
    
    private static interface IDecodeAudioFunction {
        boolean decodeAudioFrame(IPacketWrapper packet, IFrameWrapper frame) throws LibavException;
    }
    
    private static interface IEncodeAudioFunction {
        boolean encodeAudioFrame(IFrameWrapper frame, IPacketWrapper packet) throws LibavException;
    }
    
    private class DecodeAudio3 implements IDecodeAudioFunction {
        @Override
        public boolean decodeAudioFrame(IPacketWrapper packet, IFrameWrapper frame) throws LibavException {
            intByRef.setInt(frame.getLineSize().get(0));

            int packetSize = packet.getSize();
            int len = codecLib.avcodec_decode_audio3(getPointer(), frame.getData().get(0).as(Short.class), intByRef, packet.getPointer());
            if (len < 0)
                throw new LibavException(len);

            packetSize -= len;
            packet.setSize(packetSize);
            packet.setData(packetSize <= 0 ? null : packet.getData().offset(len));
            if (intByRef.getInt() > 0) {
                frame.clearWrapperCache();
                frame.getLineSize().set(0, intByRef.getInt());
                frame.setPacketPts(packet.getPts());
                frame.setPacketDts(packet.getDts());
                return true;
            }

            return false;
        }
    }
    
    private class DecodeAudio4 implements IDecodeAudioFunction {
        @Override
        public boolean decodeAudioFrame(IPacketWrapper packet, IFrameWrapper frame) throws LibavException {
            intByRef.setInt(0);
            
            frame.getDefaults();
            int packetSize = packet.getSize();
            int len = codecLib.avcodec_decode_audio4(getPointer(), frame.getPointer(), intByRef, packet.getPointer());
            if (len < 0)
                throw new LibavException(len);
            
            packetSize -= len;
            packet.setSize(packetSize);
            packet.setData(packetSize <= 0 ? null : packet.getData().offset(len));
            if (intByRef.getInt() != 0) {
                SampleFormat sf = getSampleFormat();
                int lineSize = frame.getNbSamples() * sf.getBytesPerSample();
                if (!sf.isPlanar())
                    lineSize *= getChannels();
                frame.getLineSize().set(0, lineSize);
                return true;
            }

            return false;
        }
    }
    
    private class EncodeAudio implements IEncodeAudioFunction {
        @Override
        public boolean encodeAudioFrame(IFrameWrapper frame, IPacketWrapper packet) throws LibavException {
            int bufSize = outputBufferSize;
            if (getFrameSize() <= 1) // if it is an PCM encoder
                bufSize = frame == null ? 0 : frame.getLineSize().get(0);
            if (bufSize > outputBufferSize && outputBuffer != null) {
                utilLib.av_free(outputBuffer);
                outputBuffer = null;
            }
            
            if (outputBuffer == null)
                outputBuffer = utilLib.av_malloc(outputBufferSize).as(Byte.class);
            if (outputBuffer == null)
                throw new OutOfMemoryError("not enough memory for the video frame encoder");
            
            packet.init();
            packet.setData(outputBuffer);
            packet.setSize(outputBufferSize);
            
            int len = codecLib.avcodec_encode_audio(getPointer(), outputBuffer, bufSize, frame == null ? null : frame.getData().get(0).as(Short.class));
            if (len < 0)
                throw new LibavException(len);
            else if (len == 0)
                return false;

            packet.setSize(len);

            return true;
        }
    }
    
    private class EncodeAudio2 implements IEncodeAudioFunction {
        @Override
        public boolean encodeAudioFrame(IFrameWrapper frame, IPacketWrapper packet) throws LibavException {
            intByRef.setInt(0);
            
            int len = codecLib.avcodec_encode_audio2(getPointer(), packet.getPointer(), frame == null ? null : frame.getPointer(), intByRef);
            if (len < 0)
                throw new LibavException(len);
            
            if (intByRef.getInt() != 0)
                return true;
            
            return false;
        }
    }
    
}
