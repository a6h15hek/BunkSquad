package com.socialapp.antariksh.bunksquad;

import android.graphics.*;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;

public class TextDrawableForImageView extends ShapeDrawable {

    private final Paint textPaint;
    private final Paint borderPaint;
    private static final float SHADE_FACTOR = 0.9f;
    private final String text;
    private final int color;
    private final RectShape shape;
    private final int height;
    private final int width;
    private final int fontSize;
    private final float radius;
    private final int borderThickness;

    private TextDrawableForImageView(TextDrawableForImageView.Builder builder) {
        super(builder.shape);

        // shape properties
        shape = builder.shape;
        height = builder.height;
        width = builder.width;
        radius = builder.radius;

        // text and color
        text = builder.toUpperCase ? builder.text.toUpperCase() : builder.text;
        color = builder.color;

        // text paint settings
        fontSize = builder.fontSize;
        textPaint = new Paint();
        textPaint.setColor(builder.textColor);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(builder.isBold);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStrokeWidth(builder.borderThickness);

        // border paint settings
        borderThickness = builder.borderThickness;
        borderPaint = new Paint();
        borderPaint.setColor(getDarkerShade(color));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderThickness);

        // drawable paint color
        Paint paint = getPaint();
        paint.setColor(color);

    }

    private int getDarkerShade(int color) {
        return Color.rgb((int)(SHADE_FACTOR * Color.red(color)),
                (int)(SHADE_FACTOR * Color.green(color)),
                (int)(SHADE_FACTOR * Color.blue(color)));
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Rect r = getBounds();


        // draw border
        if (borderThickness > 0) {
            drawBorder(canvas);
        }

        int count = canvas.save();
        canvas.translate(r.left, r.top);

        // draw text
        int width = this.width < 0 ? r.width() : this.width;
        int height = this.height < 0 ? r.height() : this.height;
        int fontSize = this.fontSize < 0 ? (Math.min(width, height) / 2) : this.fontSize;
        textPaint.setTextSize(fontSize);
        canvas.drawText(text, width / 2, height / 2 - ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);

        canvas.restoreToCount(count);

    }

    private void drawBorder(Canvas canvas) {
        RectF rect = new RectF(getBounds());
        rect.inset(borderThickness/2, borderThickness/2);

        if (shape instanceof OvalShape) {
            canvas.drawOval(rect, borderPaint);
        }
        else if (shape instanceof RoundRectShape) {
            canvas.drawRoundRect(rect, radius, radius, borderPaint);
        }
        else {
            canvas.drawRect(rect, borderPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        textPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    public static TextDrawableForImageView.IShapeBuilder builder() {
        return new TextDrawableForImageView.Builder();
    }

    public static class Builder implements TextDrawableForImageView.IConfigBuilder, TextDrawableForImageView.IShapeBuilder, TextDrawableForImageView.IBuilder {

        private String text;

        private int color;

        private int borderThickness;

        private int width;

        private int height;

        private Typeface font;

        private RectShape shape;

        public int textColor;

        private int fontSize;

        private boolean isBold;

        private boolean toUpperCase;

        public float radius;

        private Builder() {
            text = "";
            color = Color.GRAY;
            textColor = Color.WHITE;
            borderThickness = 0;
            width = -1;
            height = -1;
            shape = new RectShape();
            font = Typeface.create("sans-serif-light", Typeface.NORMAL);
            fontSize = -1;
            isBold = false;
            toUpperCase = false;
        }


        public TextDrawableForImageView.IConfigBuilder width(int width) {
            this.width = width;
            return this;
        }

        public TextDrawableForImageView.IConfigBuilder height(int height) {
            this.height = height;
            return this;
        }

        public TextDrawableForImageView.IConfigBuilder textColor(int color) {
            this.textColor = color;
            return this;
        }

        public TextDrawableForImageView.IConfigBuilder withBorder(int thickness) {
            this.borderThickness = thickness;
            return this;
        }

        public TextDrawableForImageView.IConfigBuilder useFont(Typeface font) {
            this.font = font;
            return this;
        }

        public TextDrawableForImageView.IConfigBuilder fontSize(int size) {
            this.fontSize = size;
            return this;
        }

        public TextDrawableForImageView.IConfigBuilder bold() {
            this.isBold = true;
            return this;
        }

        public TextDrawableForImageView.IConfigBuilder toUpperCase() {
            this.toUpperCase = true;
            return this;
        }

        @Override
        public TextDrawableForImageView.IConfigBuilder beginConfig() {
            return this;
        }

        @Override
        public TextDrawableForImageView.IShapeBuilder endConfig() {
            return this;
        }

        @Override
        public TextDrawableForImageView.IBuilder rect() {
            this.shape = new RectShape();
            return this;
        }

        @Override
        public TextDrawableForImageView.IBuilder round() {
            this.shape = new OvalShape();
            return this;
        }

        @Override
        public TextDrawableForImageView.IBuilder roundRect(int radius) {
            this.radius = radius;
            float[] radii = {radius, radius, radius, radius, radius, radius, radius, radius};
            this.shape = new RoundRectShape(radii, null, null);
            return this;
        }

        @Override
        public TextDrawableForImageView buildRect(String text, int color) {
            rect();
            return build(text, color);
        }

        @Override
        public TextDrawableForImageView buildRoundRect(String text, int color, int radius) {
            roundRect(radius);
            return build(text, color);
        }

        @Override
        public TextDrawableForImageView buildRound(String text, int color,int textColor) {
            round();
            this.textColor=textColor;
            return build(text, color);
        }

        @Override
        public TextDrawableForImageView build(String text, int color) {
            this.color = color;
            this.text = text;
            return new TextDrawableForImageView(this);
        }

    }

    public interface IConfigBuilder {
        public TextDrawableForImageView.IConfigBuilder width(int width);

        public TextDrawableForImageView.IConfigBuilder height(int height);

        public TextDrawableForImageView.IConfigBuilder textColor(int color);

        public TextDrawableForImageView.IConfigBuilder withBorder(int thickness);

        public TextDrawableForImageView.IConfigBuilder useFont(Typeface font);

        public TextDrawableForImageView.IConfigBuilder fontSize(int size);

        public TextDrawableForImageView.IConfigBuilder bold();

        public TextDrawableForImageView.IConfigBuilder toUpperCase();

        public TextDrawableForImageView.IShapeBuilder endConfig();
    }

    public static interface IBuilder {

        public TextDrawableForImageView build(String text, int color);
    }

    public static interface IShapeBuilder {

        public TextDrawableForImageView.IConfigBuilder beginConfig();

        public TextDrawableForImageView.IBuilder rect();

        public TextDrawableForImageView.IBuilder round();

        public TextDrawableForImageView.IBuilder roundRect(int radius);

        public TextDrawableForImageView buildRect(String text, int color);

        public TextDrawableForImageView buildRoundRect(String text, int color, int radius);

        public TextDrawableForImageView buildRound(String text, int color,int textColor);

    }
}
