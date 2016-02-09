package  espdisco.hodor.ninja.espdisco;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LedView extends View {
    private Paint paint;


    public LedView(Context context) {
        super(context);
        init(context);
    }

    public LedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        // create the Paint and set its color
        paint = new Paint();
        paint.setColor(Color.YELLOW);
    }

    public void changeColor(int color){
        paint.setColor(color);
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 100, paint);
    }

}