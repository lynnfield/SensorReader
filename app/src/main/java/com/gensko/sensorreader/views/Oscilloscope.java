package com.gensko.sensorreader.views;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.gensko.sensorreader.utils.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Genovich V.V. on 12.08.2015.
 */
public class Oscilloscope extends View {
    private int length = 100;
    private List<Graph> graphs = new ArrayList<>();
    private Paint paint = new Paint();

    public Oscilloscope(Context context) {
        super(context);
        paint.setColor(Color.BLACK);
    }

    public Oscilloscope(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
    }

    public Oscilloscope(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setColor(Color.BLACK);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Oscilloscope(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        paint.setColor(Color.BLACK);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void addGraph(Graph graph) {
        graphs.add(graph);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float step = calculateStep(canvas.getWidth());
        long accumulator = 0l;

        for (Graph graph : graphs) {
            accumulator += graph.size();
            drawGraph(canvas, graph, step);
        }

        canvas.drawText(
                String.format("size: %d KB", accumulator * Float.SIZE / Byte.SIZE / 1024),
                5f,
                20f,
                paint);
    }

    private void drawGraph(Canvas canvas, Graph graph, float step) {
        if (graph == null || graph.isEmpty()) return;
        float k = calculateK(canvas.getHeight(), graph.getHeight());
        float b = calculateB(k, graph.getHeight());
        List<Float> graphData = graph.getLast(length);

        float X = 0f;
        float Y = graphData.get(0) * k + b;

        for (int i = 1; i < graphData.size(); ++ i) {
            float nextX = step * (i);
            float nextY = graphData.get(i) * k + b;

            canvas.drawLine(X, Y, nextX, nextY, graph.getPaint());

            X = nextX;
            Y = nextY;
        }
    }

    private static float calculateB(float k, float graphHeight) {
        return graphHeight / 2f * k;
    }

    private static float calculateK(float canvasHeight, float graphHeight) {
        return canvasHeight / graphHeight;
    }

    private float calculateStep(float canvasWidth) {
        return canvasWidth / length;
    }
}
