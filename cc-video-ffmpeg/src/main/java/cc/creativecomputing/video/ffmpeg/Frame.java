package cc.creativecomputing.video.ffmpeg;

import org.bytedeco.javacpp.*;

import java.nio.*;

/**
 * A class to manage the data of audio and video frames. It it used by
 * {@link CanvasFrame}, {@link FrameGrabber}, {@link FrameRecorder}, and their
 * subclasses. We can also make the link with other APIs, such as Android,
 * Java 2D, FFmpeg, and OpenCV, via a {@link FrameConverter}.
 *
 * @author Samuel Audet
 */
public class Frame {
    /** A flag set by a FrameGrabber or a FrameRecorder to indicate a key frame. */
    public boolean keyFrame;

    /** Constants to be used for {@link #imageDepth}. */
    public static final int
            DEPTH_BYTE   =  -8,
            DEPTH_UBYTE  =   8,
            DEPTH_SHORT  = -16,
            DEPTH_USHORT =  16,
            DEPTH_INT    = -32,
            DEPTH_LONG   = -64,
            DEPTH_FLOAT  =  32,
            DEPTH_DOUBLE =  64;

    /** Information associated with the {@link #image} field. */
    public int imageWidth, imageHeight, imageDepth, imageChannels, imageStride;

    /**
     * Buffers to hold image pixels from multiple channels for a video frame.
     * Most of the software supports packed data only, but an array is provided
     * to allow users to store images in a planar format as well.
     */
    public Buffer[] image;

    /** Information associated with the {@link #samples} field. */
    public int sampleRate, audioChannels;

    /** Buffers to hold audio samples from multiple channels for an audio frame. */
    public Buffer[] samples;

    /** The underlying data object, for example, AVFrame, IplImage, or Mat. */
    public Object opaque;

    /** Timestamp of the frame creation. */
    public long timestamp;

    /** Empty constructor. */
    public Frame() { }

    /** Allocates a new packed image frame in native memory where rows are 8-byte aligned. */
    public Frame(int width, int height, int depth, int channels) {
        int pixelSize = Math.abs(depth) / 8;
        this.imageWidth = width;
        this.imageHeight = height;
        this.imageDepth = depth;
        this.imageChannels = channels;
        this.imageStride = ((imageWidth * imageChannels * pixelSize + 7) & ~7) / pixelSize; // 8-byte aligned
        this.image = new Buffer[1];

        ByteBuffer buffer = ByteBuffer.allocateDirect(imageHeight * imageStride * pixelSize).order(ByteOrder.nativeOrder());
        switch (imageDepth) {
            case DEPTH_BYTE:
            case DEPTH_UBYTE:  image[0] = buffer;                  break;
            case DEPTH_SHORT:
            case DEPTH_USHORT: image[0] = buffer.asShortBuffer();  break;
            case DEPTH_INT:    image[0] = buffer.asIntBuffer();    break;
            case DEPTH_LONG:   image[0] = buffer.asLongBuffer();   break;
            case DEPTH_FLOAT:  image[0] = buffer.asFloatBuffer();  break;
            case DEPTH_DOUBLE: image[0] = buffer.asDoubleBuffer(); break;
            default: throw new UnsupportedOperationException("Unsupported depth value: " + imageDepth);
        }
    }

    /**Care must be taken if this method is to be used in conjunction with movie recordings.
     *  Cloning a frame containing a full HD picture (alpha channel included) would take 1920 x 1080 * 4 = 8.294.400 Bytes.
     *  Expect a heap overflow exception when using this method without cleaning up.
     *
     * @return A deep copy of this frame.
     * @see {@link #cloneBufferArray}
     *
     * @author Extension proposed by Dragos Dutu
     * */
    @Override
    public Frame clone() {
        Frame newFrame = new Frame();


        // Video part
        newFrame.imageWidth = imageWidth;
        newFrame.imageHeight = imageHeight;
        newFrame.imageDepth = imageDepth;
        newFrame.imageChannels = imageChannels;
        newFrame.imageStride = imageStride;
        newFrame.keyFrame = keyFrame;
        newFrame.opaque = opaque;
        newFrame.image = cloneBufferArray(image);

        // Audio part
        newFrame.audioChannels = audioChannels;
        newFrame.sampleRate = sampleRate;
        newFrame.samples = cloneBufferArray(samples);

        // Add timestamp
        newFrame.timestamp = timestamp;

        return newFrame;

    }

    /**
     * This private method takes a buffer array as input and returns a deep copy.
     * It is assumed that all buffers in the input array are of the same subclass.
     *
     * @param srcBuffers - Buffer array to be cloned
     * @return New buffer array
     *
     *  @author Extension proposed by Dragos Dutu
     */
    private static Buffer[] cloneBufferArray(Buffer[] srcBuffers) {

        Buffer[] clonedBuffers = null;
        int i;
        short dataSize;

        if (srcBuffers != null) {
            clonedBuffers = new Buffer[srcBuffers.length];

            for (i = 0; i < srcBuffers.length; i++)
                srcBuffers[i].rewind();

            /*
             * In order to optimize the transfer we need a type check.
             *
             * Most CPUs support hardware memory transfer for different data
             * types, so it's faster to copy more bytes at once rather
             * than one byte per iteration as in case of ByteBuffer.
             *
             * For example, Intel CPUs support MOVSB (byte transfer), MOVSW
             * (word transfer), MOVSD (double word transfer), MOVSS (32 bit
             * scalar single precision floating point), MOVSQ (quad word
             * transfer) and so on...
             *
             * Type checking may be improved by changing the order in
             * which a buffer is checked against. If it's likely that the
             * expected buffer is of type "ShortBuffer", then it should be
             * checked at first place.
             *
             */

            if (srcBuffers[0] instanceof ByteBuffer)
                // dataSize is 1
                for (i = 0; i < srcBuffers.length; i++)
                    clonedBuffers[i] = ByteBuffer.allocateDirect(srcBuffers[i].capacity())
                            .put((ByteBuffer) srcBuffers[i]).rewind();
            else if (srcBuffers[0] instanceof ShortBuffer) {
                dataSize = Short.SIZE >> 3; // dataSize is 2
                for (i = 0; i < srcBuffers.length; i++)
                    clonedBuffers[i] = ByteBuffer.allocateDirect(srcBuffers[i].capacity() * dataSize)
                            .order(ByteOrder.nativeOrder()).asShortBuffer().put((ShortBuffer) srcBuffers[i]).rewind();
            } else if (srcBuffers[0] instanceof IntBuffer) {
                dataSize = Integer.SIZE >> 3; // dataSize is 4
                for (i = 0; i < srcBuffers.length; i++)
                    clonedBuffers[i] = ByteBuffer.allocateDirect(srcBuffers[i].capacity() * dataSize)
                            .order(ByteOrder.nativeOrder()).asIntBuffer().put((IntBuffer) srcBuffers[i]).rewind();
            } else if (srcBuffers[0] instanceof LongBuffer) {
                dataSize = Long.SIZE >> 3; // dataSize is 8
                for (i = 0; i < srcBuffers.length; i++)
                    clonedBuffers[i] = ByteBuffer.allocateDirect(srcBuffers[i].capacity() * dataSize)
                            .order(ByteOrder.nativeOrder()).asLongBuffer().put((LongBuffer) srcBuffers[i]).rewind();
            } else if (srcBuffers[0] instanceof FloatBuffer) {
                dataSize = Float.SIZE >> 3; // dataSize is 4
                for (i = 0; i < srcBuffers.length; i++)
                    clonedBuffers[i] = ByteBuffer.allocateDirect(srcBuffers[i].capacity() * dataSize)
                            .order(ByteOrder.nativeOrder()).asFloatBuffer().put((FloatBuffer) srcBuffers[i]).rewind();
            } else if (srcBuffers[0] instanceof DoubleBuffer) {
                dataSize = Double.SIZE >> 3; // dataSize is 8
                for (i = 0; i < srcBuffers.length; i++)
                    clonedBuffers[i] = ByteBuffer.allocateDirect(srcBuffers[i].capacity() * dataSize)
                            .order(ByteOrder.nativeOrder()).asDoubleBuffer().put((DoubleBuffer) srcBuffers[i]).rewind();
            }

            for (i = 0; i < srcBuffers.length; i++)
                srcBuffers[i].rewind();

        }

        return clonedBuffers;

    }

}