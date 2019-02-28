package ch.supsi.ist.camre.paths;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;

import ch.supsi.ist.camre.paths.data.OverviewElement;
import ch.supsi.ist.camre.paths.data.Path;

/**
 * Created by milan antonovic on 16/10/14.
 */
public class PathViewerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Context context;
    private Path path;
    private Paint paint;

    GameLoopThread gameLoop;

    private double lineLength;
    private float meteres;
    //private ArrayList<Double> distanceAlong;

    private ArrayList<OverviewElement> surfaces;
    private ArrayList<OverviewElement> left;
    private ArrayList<OverviewElement> right;

    private float selectionX = 0;
    private float selectionY = 0;
    private boolean selection = false;

    //These two constants specify the minimum and maximum zoom
    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 4f;

    private float scaleFactor = 1.f;
    private ScaleGestureDetector detector;

    //These constants specify the mode that we're in
    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;
    private static int SELECT = 3;

    private int mode;
    //This flag reflects whether the finger was actually dragged across the screen
    private boolean dragged = true;

    private float displayWidth;
    private float displayHeight;
    private float heightCenter;
    private float widthCenter;

    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0f;
    private float startY = 0f;

    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 0f;
    private float translateY = 0f;

    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

            if((translateX * -1) < 0) {
                translateX = 0;
            }

            //This is where we take care of the right bound. We compare translateX times -1 to (scaleFactor - 1) * displayWidth.
            //If translateX is greater than that value, then we know that we've gone over the bound. So we set the value of
            //translateX to (1 - scaleFactor) times the display width. Notice that the terms are interchanged; it's the same
            //as doing -1 * (scaleFactor - 1) * displayWidth
            else if((translateX * -1) > (scaleFactor - 1) * displayWidth) {
                translateX = (1 - scaleFactor) * displayWidth;
            }

            if(translateY * -1 < 0) {
                translateY = 0;
            }

            //We do the exact same thing for the bottom bound, except in this case we use the height of the display
            else if((translateY * -1) > (scaleFactor - 1) * displayHeight) {
                translateY = (1 - scaleFactor) * displayHeight;
            }else{
                translateY = ((1 - scaleFactor) * displayHeight)/2;
            }

            return true;
        }
    }

    public interface OnElementSelectedListener {
        // TODO: Update argument type and name
        public void onElementSelected(OverviewElement element);
        public void onElementDeSelected();
        public void onOverviewCentered(int meters);
        public void onReady();
    }

    public OnElementSelectedListener elementSelectedListener;

    public void setElementSelectedListener(OnElementSelectedListener elementSelectedListener){
        this.elementSelectedListener = elementSelectedListener;
    }

    public PathViewerSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public PathViewerSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PathViewerSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){

        System.out.println("PathViewerSurfaceView: initialzation");

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        paint = new Paint();

        displayWidth = display.getWidth();
        displayHeight = display.getHeight();
        heightCenter = displayHeight / 2;

        detector = new ScaleGestureDetector(getContext(), new ScaleListener());

        gameLoop = new GameLoopThread(this);

        //this.distanceAlong = new ArrayList<Double>();

        this.surfaces = new ArrayList<OverviewElement>();
        this.left = new ArrayList<OverviewElement>();
        this.right = new ArrayList<OverviewElement>();

        this.lineLength = 0;

        this.context = context;

        getHolder().addCallback(this);

    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setLength(double lineLength){
        this.lineLength = lineLength;
        this.meteres = (float) (lineLength / 2);
    }

    public double getLength(){
        return this.lineLength;
    }

    public void addSurface(OverviewElement overviewElement){
        this.surfaces.add(overviewElement);
    }

    public  ArrayList<OverviewElement> getSurface(){
        return this.surfaces;
    }

    public void addLeftSide(OverviewElement overviewElement){
        this.left.add(overviewElement);
    }

    public ArrayList<OverviewElement> getLeftSide(){
        return this.left;
    }


    public void addRightSide(OverviewElement overviewElement){
        this.right.add(overviewElement);
    }

    public  ArrayList<OverviewElement> getRightSide(){
        return this.right;
    }

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        android.graphics.Path p;

        displayWidth = canvas.getWidth();
        displayHeight = canvas.getHeight();
        heightCenter = displayHeight / 2;
        widthCenter = displayWidth / 2;


        canvas.save();


        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setAntiAlias(true);
        canvas.drawColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        //canvas.drawText("Path lenght: " + String.valueOf((double)Math.round(getLength() * 100) / 100) + " m", 20, 30, paint);

        if (scaleFactor>1.f){

            paint.setColor(Color.RED);

            canvas.drawText(Math.round(this.meteres) + " m", widthCenter+5, 30, paint);

            paint.setStrokeWidth(1f);

            paint.setStyle(Paint.Style.STROKE);
            paint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));

            p = new android.graphics.Path();
            p.moveTo(widthCenter, 0f);
            p.lineTo(widthCenter, displayHeight);
            canvas.drawPath(p, paint);

            paint.setPathEffect(null);

        }


        //We're going to scale the X and Y coordinates by the same amount
        canvas.scale(scaleFactor, scaleFactor);
        //We need to divide by the scale factor here, otherwise we end up with excessive panning based on our zoom level
        //because the translation amount also gets scaled according to how much we've zoomed into the canvas.
        canvas.translate(translateX / scaleFactor, translateY / scaleFactor);

        /*if (selection){
            paint.setColor(Color.RED);
            canvas.drawCircle( selectionX / scaleFactor,selectionY / scaleFactor,10, paint);
        }*/

        //if (this.surfaces.size()==path.getSurface().size()){

        if (this.surfaces.size()==path.getSurface().size() &&
                this.left.size()==path.getLeftSide().size() &&
                this.right.size()==path.getRightSide().size()){

            boolean alternate = true;

            float height = canvas.getHeight(),
                    width = canvas.getWidth(),
                    centerHeight = height/2;

            float surfaceBegin = 0f;
            RectF block = new RectF();

            for (OverviewElement surface: surfaces){

                float start = width*surfaceBegin;
                float end = width*surface.getPercentTo();

                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(0.25f);

                p = new android.graphics.Path();
                p.moveTo(end, 0f);
                p.lineTo(end, height);
                canvas.drawPath(p, paint);

                paint.setStrokeWidth(1f);
                paint.setColor(Color.BLACK);
                paint.setTextSize(30 / scaleFactor);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);

                //System.out.println("Canvas: " + height + " translateY: " + translateY + " scaleFactor: " + scaleFactor);
                //System.out.println(" > translateY / scaleFactor: " + (translateY / scaleFactor));

                int pad = 5;
                if((surfaces.size()-1)==surfaces.indexOf(surface)){
                    paint.setTextAlign(Paint.Align.RIGHT);
                    pad = -10;
                    canvas.drawText(Math.round(surface.getMetersTo()) + " m", end + pad, (-translateY + 50)/ scaleFactor, paint);
                }else{
                    paint.setTextAlign(Paint.Align.LEFT);
                    if (alternate) {
                        canvas.drawText(Math.round(surface.getMetersTo()) + " m", end + pad, (-translateY + 100)/ scaleFactor, paint);
                    } else {
                        canvas.drawText(Math.round(surface.getMetersTo()) + " m", end + pad, (-translateY + 150) / scaleFactor, paint);
                    }
                }

                paint.setTextAlign(Paint.Align.LEFT);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.TRANSPARENT);

                surface.setRect(start, centerHeight - 10f,
                        end, centerHeight + 10f);

                int fillColor, strokeColor;
                if (selection && surface.getRect()
                        .contains(selectionX / scaleFactor, selectionY / scaleFactor)){

                    fillColor = Color.BLACK;
                    strokeColor = Color.WHITE;

                    paint.setColor(Color.BLACK);
                    paint.setColor(fillColor);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);

                    canvas.drawRect(surface.getRect(), paint);
                    paint.setColor(Color.WHITE);
                }else{
                    fillColor = Color.WHITE;
                    strokeColor = Color.BLACK;
                }
                paint.setColor(strokeColor);
                paint.setStrokeWidth(1f);


                int div = (int)((end - start)/20);
                float pix = ((end - start)/div);

                //paint.setColor(Color.BLACK);

                canvas.drawLine(start, centerHeight,
                        end, centerHeight, paint);

                for (int c = 0; c < div; c++){

                    block.set(start+(c*pix), centerHeight - 10f,
                            start+(c*pix)+20, centerHeight + 10f);

                    if (surface.getElement().getType().equals(
                            "http://www.camre.ch/properties/surface/pavement")) {

                        // Cerchio con bordo

                        paint.setColor(fillColor);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(block.centerX(), block.centerY(), 6f, paint);

                        paint.setColor(strokeColor);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(block.centerX(), block.centerY(), 6f, paint);


                    }else if(surface.getElement().getType().equalsIgnoreCase(
                            "http://www.camre.ch/properties/surface/gravel")){

                        // Piccolo cerchio senza bordo

                        paint.setColor(strokeColor);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(block.centerX(), block.centerY(), 3f, paint);

                        /*paint.setColor(Color.BLACK);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(block.centerX(), block.centerY(), 3f, paint);*/


                    /*}else if(surface.getElement().getType().equalsIgnoreCase(
                            "http://www.camre.ch/properties/surface/asphalt")){*/

                    }else{

                        // Quadrato ruotato di 45° <>

                        paint.setColor(fillColor);
                        paint.setStyle(Paint.Style.FILL);

                        p = new android.graphics.Path();
                        p.moveTo(block.centerX()-6, block.centerY());
                        p.lineTo(block.centerX(), block.centerY()-6);
                        p.lineTo(block.centerX()+6, block.centerY());
                        p.lineTo(block.centerX(), block.centerY()+6);
                        p.lineTo(block.centerX()-6, block.centerY());
                        canvas.drawPath(p, paint);

                        paint.setColor(strokeColor);
                        paint.setStyle(Paint.Style.STROKE);

                        p = new android.graphics.Path();
                        p.moveTo(block.centerX()-6, block.centerY());
                        p.lineTo(block.centerX(), block.centerY()-6);
                        p.lineTo(block.centerX()+6, block.centerY());
                        p.lineTo(block.centerX(), block.centerY()+6);
                        p.lineTo(block.centerX()-6, block.centerY());
                        canvas.drawPath(p, paint);

                    }

                }

                surfaceBegin = surface.getPercentTo();
                alternate = !alternate;
            }

            for (OverviewElement leftSide: left){

                float begin = leftSide.getPercentFrom();
                float end = leftSide.getPercentTo();

                float left = width*begin;
                float right = width*end;

                float offset = 40f;
                int multiplier = 0;

                for (int c = this.left.indexOf(leftSide)-1; c >= 0; c--){
                    OverviewElement prev = this.left.get(c);
                    if(begin<prev.getPercentTo() && begin>=prev.getPercentFrom()){
                        multiplier++;
                    }
                }

                float top = centerHeight - 30f - (offset*multiplier);
                float bottom = centerHeight - 60f - (offset*multiplier);
                float middle = (top + bottom ) / 2;

                leftSide.setRect(left, bottom,
                        right, top);

                if (selection && leftSide.getRect()
                        .contains(selectionX / scaleFactor, selectionY / scaleFactor)){
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawRect(leftSide.getRect(), paint);
                    paint.setColor(Color.WHITE);
                }else{
                    paint.setColor(Color.BLACK);
                }
                paint.setStrokeWidth(2f);

                /*canvas.drawRect(
                        left, top,
                        right, bottom,
                        paint);*/

                alternate = !alternate;

                int div = (int)((right - left)/20);
                float pix = ((right - left)/div);

                if (!leftSide.getElement().getType().equals(
                        "http://www.camre.ch/properties/woods")){
                    canvas.drawLine(
                            left, middle,
                            right, middle,
                            paint);
                }

                for (int c = 0; c < div; c++) {

                    block.set(left + (c * pix), middle,
                            left + (c * pix) + 20, middle);

                    if (leftSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/scarp/terrain/down")) {

                        // -------------------------
                        //  |   |   |   |   |   |

                        paint.setStyle(Paint.Style.STROKE);

                        p = new android.graphics.Path();
                        p.moveTo(block.centerX(), block.centerY());
                        p.lineTo(block.centerX(), block.centerY()+10);
                        canvas.drawPath(p, paint);

                    }else if (leftSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/scarp/terrain/up")) {

                        //  |   |   |   |   |   |
                        // -------------------------

                        paint.setStyle(Paint.Style.STROKE);

                        p = new android.graphics.Path();
                        p.moveTo(block.centerX(), block.centerY());
                        p.lineTo(block.centerX(), block.centerY()-10);
                        canvas.drawPath(p, paint);

                    }else if (leftSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/wall/stone")) {

                        // -------------------------
                        //  #   #   #   #   #   #   (quadratini)

                        paint.setStyle(Paint.Style.FILL);

                        canvas.drawRect(
                                block.left+5, block.top,
                                block.right-5, block.bottom+10,
                                paint);

                    }else if (leftSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/wall/cement")) {

                        // -------------------------
                        //  \/\/\/\/\/\/\/\/\/\/\/\/ (triangolini)

                        paint.setStyle(Paint.Style.FILL);

                        p = new android.graphics.Path();
                        p.moveTo(block.left, middle);
                        p.lineTo(block.centerX(), middle + 10);
                        p.lineTo(block.right, middle);
                        p.lineTo(block.left, middle);

                        canvas.drawPath(p, paint);

                    }else if (leftSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/fence/wood")) {

                        //  x-x-x-x-x-x-x-x-x-x-x-x- (Iks)

                        paint.setStyle(Paint.Style.STROKE);

                        p = new android.graphics.Path();
                        p.moveTo(block.left + 5, block.bottom - 5);
                        p.lineTo(block.right - 5, block.top + 5);
                        p.moveTo(block.right - 5, block.bottom - 5);
                        p.lineTo(block.left + 5, block.top + 5);

                        canvas.drawPath(p, paint);


                    }else if (leftSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/woods")) {

                        //  o o o o o o o o o o o o (pallini)

                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(block.centerX(), block.centerY(), 6f, paint);


                    }
                }

            }

            for (OverviewElement rightSide: right){

                float begin = rightSide.getPercentFrom();
                float end = rightSide.getPercentTo();

                float left = width*begin;
                float right = width*end;

                float offset = 40f;
                int multiplier = 0;

                for (int c = this.right.indexOf(rightSide)-1; c >= 0; c--){
                    OverviewElement prev = this.right.get(c);
                    if(begin<prev.getPercentTo() && begin>=prev.getPercentFrom()){
                        multiplier++;
                    }
                }

                float top = centerHeight + 30f + (offset*multiplier);
                float bottom = centerHeight + 60f + (offset*multiplier);
                float middle = (top + bottom ) / 2;

                rightSide.setRect(left, top,
                        right, bottom);

                if (selection && rightSide.getRect()
                        .contains(selectionX / scaleFactor, selectionY / scaleFactor)){
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawRect(rightSide.getRect(), paint);
                    paint.setColor(Color.WHITE);
                    paint.setStrokeWidth(2f);
                }else{
                    paint.setColor(Color.BLACK);
                    paint.setStrokeWidth(2f);
                }
                alternate = !alternate;


                int div = (int)((right - left)/20);
                float pix = ((right - left)/div);


                if (!rightSide.getElement().getType().equalsIgnoreCase(
                        "http://www.camre.ch/properties/woods")){
                    canvas.drawLine(
                            left, middle,
                            right, middle,
                            paint);
                }

                for (int c = 0; c < div; c++) {

                    block.set(left + (c * pix), middle,
                            left + (c * pix) + 20, middle);

                    if (rightSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/scarp/terrain/down")) {

                        //  |   |   |   |   |   |
                        // -------------------------

                        paint.setStyle(Paint.Style.STROKE);

                        p = new android.graphics.Path();
                        p.moveTo(block.centerX(), block.centerY());
                        p.lineTo(block.centerX(), block.centerY()-10);
                        canvas.drawPath(p, paint);

                    }else if (rightSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/scarp/terrain/up")) {

                        //  |   |   |   |   |   |
                        // -------------------------

                        paint.setStyle(Paint.Style.STROKE);

                        p = new android.graphics.Path();
                        p.moveTo(block.centerX(), block.centerY());
                        p.lineTo(block.centerX(), block.centerY()+10);
                        canvas.drawPath(p, paint);

                    }else if (rightSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/wall/stone")) {

                        // -------------------------
                        //  #   #   #   #   #   #   (quadratini)

                        paint.setStyle(Paint.Style.FILL);

                        canvas.drawRect(
                                block.left+5, block.top-10,
                                block.right-5, block.bottom,
                                paint);

                    }else if (rightSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/wall/cement")) {

                        // -------------------------
                        //  \/\/\/\/\/\/\/\/\/\/\/\/ (triangolini)

                        paint.setStyle(Paint.Style.FILL);

                        p = new android.graphics.Path();
                        p.moveTo(block.left, middle);
                        p.lineTo(block.centerX(), middle - 10);
                        p.lineTo(block.right, middle);
                        p.lineTo(block.left, middle);

                        canvas.drawPath(p, paint);

                    }else if (rightSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/fence/wood")) {

                        //  x-x-x-x-x-x-x-x-x-x-x-x- (Iks)

                        paint.setStyle(Paint.Style.STROKE);

                        p = new android.graphics.Path();
                        p.moveTo(block.left + 5, block.bottom - 5);
                        p.lineTo(block.right - 5, block.top + 5);
                        p.moveTo(block.right - 5, block.bottom - 5);
                        p.lineTo(block.left + 5, block.top + 5);

                        canvas.drawPath(p, paint);


                    }else if (rightSide.getElement().getType().equals(
                            "http://www.camre.ch/properties/woods")) {

                        //  o o o o o o o o o o o o (pallini)

                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(block.centerX(), block.centerY(), 6f, paint);


                    }
                }

            }

        }


        canvas.restore();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        System.out.println("PathViewerSurfaceView: surfaceCreated");
        /*Canvas c = holder.lockCanvas(null);
        onDraw(c);
        holder.unlockCanvasAndPost(c);*/

        gameLoop.setRunning(true);
        gameLoop.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameLoop.setRunning(false);
        while (retry) {
            try {
                gameLoop.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    private class GameLoopThread extends Thread {

        static final long FPS = 15;
        private PathViewerSurfaceView view;
        private boolean running = false;

        public GameLoopThread(PathViewerSurfaceView view) {
            this.view = view;
        }

        public void setRunning(boolean run) {
            running = run;
        }

        @Override
        public void run() {
            long ticksPS = 1000 / FPS;
            long startTime;
            long sleepTime;
            while (running) {
                Canvas c = null;
                startTime = System.currentTimeMillis();
                try {
                    c = view.getHolder().lockCanvas();
                    synchronized (view.getHolder()) {
                        view.onDraw(c);
                    }
                } finally {
                    if (c != null) {
                        view.getHolder().unlockCanvasAndPost(c);
                    }
                }
                sleepTime = ticksPS-(System.currentTimeMillis() - startTime);
                try {
                    if (sleepTime > 0)
                        sleep(sleepTime);
                    else
                        sleep(10);
                } catch (Exception e) {}
            }
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                //mode = DRAG;
                mode = SELECT;

                //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
                //amount for each coordinates This works even when we are translating the first time because the initial
                //values for these two variables is zero.
                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;

                elementSelectedListener.onElementDeSelected();

                break;

            case MotionEvent.ACTION_MOVE:
                translateX = event.getX() - startX;
                //translateY = event.getY() - startY;

                //We cannot use startX and startY directly because we have adjusted their values using the previous translation values.
                //This is why we need to add those values to startX and startY so that we can get the actual coordinates of the finger.
                double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) +
                                Math.pow(event.getY() - (startY + previousTranslateY), 2)
                );

                if(distance > 0) {
                    dragged = true;

                    if((translateX * -1) < 0) {
                        translateX = 0;
                    }else if((translateX * -1) > (scaleFactor - 1) * displayWidth) {
                        translateX = (1 - scaleFactor) * displayWidth;
                    }
                }

                if (mode == SELECT){
                    mode = DRAG;
                }

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_UP:

                if (mode == SELECT){
                    selectionX = event.getX() - translateX;
                    selectionY = event.getY() - translateY;
                    selection = true;

                    // Check intersection
                    //
                    /*for (RectF rect: this.rectSurfaces){
                        if (rect.contains(selectionX / scaleFactor,selectionY / scaleFactor)){
                            System.out.println("FOUND!!!!");
                            this.elementSelectedListener.onElementSelected(this.path.getSurface().get(this.rectSurfaces.indexOf(rect)));
                            break;
                        }else {
                            System.out.println("NOT FOUND..");
                        }
                    }*/

                    ArrayList<OverviewElement> tmp = new ArrayList<OverviewElement>();
                    tmp.addAll(surfaces);
                    tmp.addAll(left);
                    tmp.addAll(right);
                    for (OverviewElement element: tmp){
                        System.out.println("Element: " + element.getElement().getType());
                        System.out.println(
                                " Left: " + element.getRect().left +
                                "  X1: " + (selectionX / scaleFactor) +
                                " Right: " + element.getRect().right +
                                " // Bottom: " + element.getRect().bottom +
                                " Y2: " + (selectionY / scaleFactor) +
                                " Top: " + element.getRect().top);
                        if (element.getRect().contains(selectionX / scaleFactor,selectionY / scaleFactor)){
                            System.out.println(" > FOUND!!!!");
                            this.elementSelectedListener.onElementSelected(element);
                            break;
                        }else {
                            System.out.println(" > NOT FOUND..");
                        }
                    }


                }else{
                    selection = false;
                }

                mode = NONE;
                dragged = false;

                //All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
                //previousTranslate
                previousTranslateX = translateX;
                //previousTranslateY = translateY;

                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = DRAG;

                //This is not strictly necessary; we save the value of translateX and translateY into previousTranslateX
                //and previousTranslateY when the second finger goes up
                previousTranslateX = translateX;
                //previousTranslateY = translateY;
                break;
        }

        detector.onTouchEvent(event);

        float w2 = (displayWidth / scaleFactor) / 2;
        float x2 = w2 - (translateX / scaleFactor);
        this.meteres = (float) (x2 / (displayWidth) * getLength());

        if(scaleFactor>1)
            this.elementSelectedListener.onOverviewCentered((int)meteres);
        else
            this.elementSelectedListener.onOverviewCentered(-1);

        //System.out.println("Scalefactor: " + scaleFactor + " Center meters: " + meteres);

        //We redraw the canvas only in the following cases:
        //
        // o The mode is ZOOM
        //        OR
        // o The mode is DRAG and the scale factor is not equal to 1 (meaning we have zoomed) and dragged is
        //   set to true (meaning the finger has actually moved)
        /*if ((mode == DRAG && scaleFactor != 1f && dragged) || mode == ZOOM) {
            //invalidate();
            System.out.println();
        }*/

        return true;
    }

    public void centerTo(int meter){

        /*float w2 = (displayWidth / scaleFactor) / 2;
        float x2 = w2 - (translateX / scaleFactor);

        translateX = (float) (
            (
                    (-scaleFactor*meter*displayWidth)/getLength()
            ) - (displayWidth / scaleFactor) / 2
        );*/

        //translateX = (float) (-4 * meter * scaleFactor * displayWidth * getLength() - displayWidth);

        //translateX = (float) ((meter/getLength())*displayWidth-((displayWidth / scaleFactor) / 2));


    }

}
