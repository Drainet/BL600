package com.lairdtech.bl600toolkit.graphs;

import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

public class HeartRateGraph extends MyGraph{
    // graph line
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
    
    public HeartRateGraph(Context context, View view){
        super(context, view);
        initChart();
        setStartingPositions(mRenderer.getXAxisMin(), mRenderer.getXAxisMax(), mRenderer.getYAxisMin(), mRenderer.getYAxisMax());
        paintChart();
    }
    
    @Override
    protected void initChart() {
        super.initChart();
        mCurrentSeries = new XYSeries("BPM");
        mRenderer.setYTitle("Heartbeat");
        mRenderer.setPanEnabled(IS_GRAPH_MOVEABLE_BY_TOUCH_HORIZONTAL, IS_GRAPH_MOVEABLE_BY_TOUCH_VERTICAL);
        mRenderer.setZoomEnabled(IS_GRAPH_ZOOMABLE_BY_TOUCH_HORIZONTAL, IS_GRAPH_ZOOMABLE_BY_TOUCH_VERTICAL);
        mRenderer.setYAxisMax(210);
        mRenderer.setYAxisMin(0);
        mDataset.addSeries(mCurrentSeries);
        // graph line
        mCurrentRenderer = new XYSeriesRenderer();
        mCurrentRenderer.setColor(Color.parseColor("#FF0000")); // FF0000
        mCurrentRenderer.setLineWidth(LINE_WEIGHT);
        mRenderer.addSeriesRenderer(mCurrentRenderer);
    }

    public void addNewData(double bpm) {
        // x, y
        mCurrentSeries.add(calculateElapsedTime(), bpm);
        paintChart();
    }
    
    @Override
    public void clearGraph(){
        mCurrentSeries.clear();
        setAxisStartingPoints();
        paintChart();
    }
}