package no.lau.vdvil.renderer.video.phun;

import com.xuggle.xuggler.Global;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class ImageCreator implements ImageCreatorI {
    private final Collection<Ball> mBalls = new Vector<>();
    private final BufferedImage mImage;
    private final Graphics2D mGraphics;
    private final short[] mSamples;

    public ImageCreator(int ballCount, int width, int height, int sampleCount) {
        while(this.mBalls.size() < ballCount) {
            this.mBalls.add(new ImageCreator.Ball(width, height));
        }

        this.mImage = new BufferedImage(width, height, 5);
        this.mGraphics = this.mImage.createGraphics();
        this.mGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.mSamples = new short[sampleCount];
    }

    public BufferedImage getVideoFrame(long elapsedTime) {
        this.mGraphics.setColor(Color.WHITE);
        this.mGraphics.fillRect(0, 0, this.mImage.getWidth(), this.mImage.getHeight());

        for (Ball ball : this.mBalls) {
            ball.update(elapsedTime);
            ball.paint(this.mGraphics);
        }

        return this.mImage;
    }

    public short[] getAudioFrame(int sampleRate) {
        for(int i$ = 0; i$ < this.mSamples.length; ++i$) {
            this.mSamples[i$] = 0;
        }

        for (Ball ball : this.mBalls) {
            ball.setAudioProgress(addSignal(ball.getFrequency(), sampleRate, 1.0D / (double) this.mBalls.size(), ball.getAudioProgress(), this.mSamples));
        }

        return this.mSamples;
    }

    public static double addSignal(int frequency, int sampleRate, double volume, double progress, short[] samples) {
        double amplitude = 32767.0D * volume;
        double epsilon = 6.283185307179586D * (double)frequency / (double)sampleRate;

        for(int i = 0; i < samples.length; ++i) {
            int sample = samples[i] + (short)((int)(amplitude * Math.sin(progress)));
            sample = Math.max(-32768, sample);
            sample = Math.min(32767, sample);
            samples[i] = (short)sample;
            progress += epsilon;
        }

        return progress;
    }

    static class Ball extends Ellipse2D.Double {
        public static final long serialVersionUID = 0L;
        private static final int MIN_FREQ_HZ = 220;
        private static final int MAX_FREQ_HZ = 880;
        private final int mWidth;
        private final int mHeight;
        private final int mRadius;
        private final double mSpeed;
        private static final Random rnd = new Random();
        private double mAngle = 0.0D;
        private Color mColor;
        private double mAudioProgress;

        public Ball(int width, int height) {
            this.mColor = Color.BLUE;
            this.mAudioProgress = 0.0D;
            this.mWidth = width;
            this.mHeight = height;
            this.mRadius = rnd.nextInt(10) + 10;
            this.mSpeed = ((double)rnd.nextInt(200) + 100.0D) / (double) Global.DEFAULT_TIME_UNIT.convert(1L, TimeUnit.SECONDS);
            this.setLocation((double)((this.mWidth - 2 * this.mRadius) / 2), (double)((this.mHeight - 2 * this.mRadius) / 2));
            this.mAngle = rnd.nextDouble() * 3.141592653589793D * 2.0D;
            this.mColor = new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }

        public void setAudioProgress(double audioProgress) {
            this.mAudioProgress = audioProgress;
        }

        public double getAudioProgress() {
            return this.mAudioProgress;
        }

        private void setLocation(double x, double y) {
            this.setFrame(x, y, (double)(2 * this.mRadius), (double)(2 * this.mRadius));
        }

        public int getFrequency() {
            double angle = (Math.toDegrees(this.mAngle) % 360.0D + 360.0D) % 360.0D;
            return (int)(angle / 360.0D * 660.0D + 220.0D);
        }

        public void update(long elapsedTime) {
            double x = this.getX() + Math.cos(this.mAngle) * this.mSpeed * (double)elapsedTime;
            double y = this.getY() + Math.sin(this.mAngle) * this.mSpeed * (double)elapsedTime;
            if(x < 0.0D || x > (double)(this.mWidth - this.mRadius * 2)) {
                this.mAngle = 3.141592653589793D - this.mAngle;
                x = this.getX();
            }

            if(y < 0.0D || y > (double)(this.mHeight - this.mRadius * 2)) {
                this.mAngle = -this.mAngle;
                y = this.getY();
            }

            this.setLocation(x, y);
        }

        public void paint(Graphics2D g) {
            g.setColor(this.mColor);
            g.fill(this);
        }
    }
}
