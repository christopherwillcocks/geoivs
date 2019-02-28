package ch.supsi.ist.camre.paths.data;

import android.graphics.RectF;

public class OverviewElement {

    private Element element;
    private RectF rect;
    private float metersTo;
    private float metersFrom;
    private float percentFrom;
    private float percentTo;

    public OverviewElement(Element element, float metersTo, float percentTo) {
        this.element = element;
        this.metersTo = metersTo;
        this.percentTo = percentTo;
        this.rect = new RectF();
    }

    public OverviewElement(Element element, float metersFrom, float metersTo, float percentFrom, float percentTo) {
        this.element = element;
        this.metersFrom = metersFrom;
        this.metersTo = metersTo;
        this.percentFrom = percentFrom;
        this.percentTo = percentTo;
        this.rect = new RectF();
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public RectF getRect() {
        return rect;
    }

    public void setRect(RectF rect) {
        this.rect = rect;
    }

    public void setRect(float left, float top, float right, float bottom) {
        this.rect.set(left, top, right,  bottom);
    }

    public float getMetersTo() {
        return metersTo;
    }

    public void setMetersTo(float metersTo) {
        this.metersTo = metersTo;
    }

    public float getPercentTo() {
        return percentTo;
    }

    public void setPercentTo(float percentTo) {
        this.percentTo = percentTo;
    }

    public float getMetersFrom() {
        return metersFrom;
    }

    public float getMeters() {
        return (float)Math.round((metersTo - metersFrom) * 100) / 100;
    }

    public void setMetersFrom(float metersFrom) {
        this.metersFrom = metersFrom;
    }

    public float getPercentFrom() {
        return percentFrom;
    }

    public void setPercentFrom(float percentFrom) {
        this.percentFrom = percentFrom;
    }
}
