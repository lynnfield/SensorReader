package com.gensko.sensorreader.utils;

import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Genovich V.V. on 14.08.2015.
 */
public class Graph extends ArrayList<Float> {
    private Paint paint = new Paint();
    private float height;
    private float zero = 0f;

    public Graph(int color, float height) {
        this.paint.setColor(color);
        this.paint.setStrokeWidth(1.5f);
        this.height = height;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getZero() {
        return zero;
    }

    public void setZero(float zero) {
        this.zero = zero;
    }

    public List<Float> getLast(int maxPoints) {
        int firstIndex = size() - maxPoints;
        firstIndex = firstIndex < 0 ? 0 : firstIndex;
        return subList(firstIndex, size());
    }

    @Override
    public Float get(int index) {
        return super.get(index) - zero;
    }
}
