package com.alradhy.discoverylab;

import android.graphics.*;

/** Original experiment illustrations, driven by the new workbench state. */
public final class LegacyLabArt {
    private static final int BG=Color.rgb(7,15,29), CARD=Color.rgb(17,34,57);
    private static final int WHITE=Color.rgb(235,245,255), GOLD=Color.rgb(246,197,99);
    private static final int MUTED=Color.rgb(158,186,212), CYAN=Color.rgb(81,219,237);
    private static final int ORANGE=Color.rgb(255,143,92), GREEN=Color.rgb(113,221,163);
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static double rad(double value) { return PhysicsModels.rad(value); }
    private static double bk7(double value) { return PhysicsModels.bk7(value); }
    private static String n(double value, int digits) { return LabCatalog.n(value, digits); }
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){
        p.setColor(color);p.setStrokeWidth(width);p.setStyle(Paint.Style.STROKE);c.drawLine(x,y,xx,yy,p);p.setStyle(Paint.Style.FILL);
    }
    private void circle(Canvas c,float x,float y,float r,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);}
    private void box(Canvas c,float x,float y,float w,float h,int color){
        p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(x,y,x+w,y+h),9,9,p);
    }
    private void caption(Canvas c,String text,float x,float y,int color,int size){
        p.setColor(color);p.setTextSize(size);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);c.drawText(text,x,y,p);
    }
    private void arrow(Canvas c,float x1,float y1,float x2,float y2,int col,float sw){
        line(c,x1,y1,x2,y2,col,sw);
        float dx=x2-x1,dy=y2-y1;
        float len=(float)Math.sqrt(dx*dx+dy*dy);
        if(len<1)return;
        float ux=dx/len,uy=dy/len;
        line(c,x2,y2,x2-ux*15-uy*9,y2-uy*15+ux*9,col,sw);
        line(c,x2,y2,x2-ux*15+uy*9,y2-uy*15-ux*9,col,sw);
    }
    private void strokeCircle(Canvas c,float x,float y,float r,int col,float sw){
        p.setColor(col);p.setStrokeWidth(sw);p.setStyle(Paint.Style.STROKE);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);
    }
    private void ellipse(Canvas c,float x,float y,float rx,float ry,int col,float sw){
        p.setColor(col);p.setStrokeWidth(sw);p.setStyle(Paint.Style.STROKE);
        c.drawOval(new RectF(x-rx,y-ry,x+rx,y+ry),p);p.setStyle(Paint.Style.FILL);
    }
    public void draw(Canvas actual, int i, double x, double y, double secondChargeMicroCoulombs, double u, double v, double t){
        Canvas c=actual;box(c,0,0,1000,440,CARD);
        for(int k=1;k<10;k++)line(c,k*100,10,k*100,430,Color.rgb(26,49,71),1);
        for(int k=1;k<5;k++)line(c,10,k*88,990,k*88,Color.rgb(26,49,71),1);
        switch(i){
            case 0:{ // BK7 prism: blue and red diverging rays
                Path prism=new Path();prism.moveTo(490,90);prism.lineTo(340,360);prism.lineTo(640,360);prism.close();
                p.setColor(Color.argb(100,93,164,213));c.drawPath(prism,p);
                p.setColor(CYAN);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);c.drawPath(prism,p);p.setStyle(Paint.Style.FILL);
                line(c,70,218,421,218,GOLD,6);
                double angle=rad(x),dB=2*Math.asin(bk7(450)*Math.sin(angle/2))-angle,
                    dR=2*Math.asin(bk7(650)*Math.sin(angle/2))-angle;
                float rY=(float)(185+160*Math.tan(dR)),bY=(float)(185+160*Math.tan(dB));
                line(c,475,205,745,260,WHITE,4);line(c,745,260,880,rY,ORANGE,5);line(c,745,260,880,bY,CYAN,5);
                line(c,896,75,896,400,MUTED,3);caption(c,"BK7",470,305,WHITE,26);caption(c,"RED",902,Math.min(420,rY),ORANGE,18);caption(c,"BLUE",902,Math.min(425,bY),CYAN,18);break;
            }
            case 1:{ // Foucault: floor precession of oscillation plane
                ellipse(c,500,345,300,70,MUTED,2);circle(c,500,76,12,GOLD);
                double orientation=rad(15.041067*Math.sin(rad(x))*y);
                double swing=Math.sin(t*2.1)*160;float endX=(float)(500+Math.cos(orientation)*swing),endY=(float)(310+Math.sin(orientation)*swing*.42);
                line(c,500,76,endX,endY,CYAN,5);circle(c,endX,endY,23,GOLD);
                float ax=(float)(500+Math.cos(orientation)*250),ay=(float)(345+Math.sin(orientation)*50);
                float bx=(float)(500-Math.cos(orientation)*250),by=(float)(345-Math.sin(orientation)*50);
                line(c,ax,ay,bx,by,ORANGE,5);caption(c,"EARTH",60,75,MUTED,22);break;
            }
            case 2:{ // Coulomb: charges and force vectors
                float apart=(float)(180+240*y/2);float left=500-apart/2,right=500+apart/2;
                double f=x*secondChargeMicroCoulombs;
                int col1=x<0?ORANGE:x>0?CYAN:MUTED;
                int col2=secondChargeMicroCoulombs<0?ORANGE:secondChargeMicroCoulombs>0?CYAN:MUTED;
                circle(c,left,215,46,col1);circle(c,right,215,46,col2);
                caption(c,x<0?"−":x>0?"+":"0",left-14,225,BG,32);
                caption(c,secondChargeMicroCoulombs<0?"−":secondChargeMicroCoulombs>0?"+":"0",right-14,225,BG,32);
                line(c,left,300,right,300,MUTED,3);
                if(f!=0){
                    float direction=f<0?1:-1;
                    float length=(float)(50+Math.min(70,Math.abs(f)*8/y));
                    arrow(c,left+direction*52,215,left+direction*(52+length),215,GOLD,6);
                    arrow(c,right-direction*52,215,right-direction*(52+length),215,GOLD,6);
                }
                caption(c,f<0?"ATTRACTION":f>0?"REPULSION":"ZERO FORCE",365,385,WHITE,27);
                caption(c,"q1 "+n(x,1)+" uC",left-80,128,col1,22);
                caption(c,"q2 "+n(secondChargeMicroCoulombs,1)+" uC",right-80,128,col2,22);break;
            }
            case 3:{ // orbital speed relative to ideal circle
                circle(c,500,217,70,CYAN);strokeCircle(c,500,217,185,MUTED,3);
                double vc=Math.sqrt(3.986004418e14/((6371+x)*1000));float radius=185;
                float px=500+radius,py=217;circle(c,px,py,14,GOLD);float vector=(float)(y*28);
                line(c,px,py,px,py-vector,ORANGE,7);
                caption(c,"EARTH",456,225,BG,18);caption(c,"v = "+n(y,1)+" km/s",665,70,GOLD,22);
                caption(c,"vc = "+n(vc/1000,1)+" km/s",85,385,WHITE,24);break;
            }
            case 4:{ // interference intensity at screen; sinusoidal fringes
                box(c,110,125,35,220,Color.rgb(50,85,122));circle(c,127,185,8,GOLD);circle(c,127,282,8,GOLD);
                line(c,145,185,795,115,CYAN,2);line(c,145,282,795,335,CYAN,2);
                box(c,790,40,62,360,Color.rgb(31,54,81));
                double period=x*1e-9*2/(y*1e-3),scale=Math.max(8,period*18000);
                for(int k=0;k<350;k+=3){
                    double pos=k-175,phase=Math.PI*pos/scale;
                    double intensity=Math.pow(Math.cos(phase),2);
                    line(c,790,42+k,852,42+k,Color.rgb((int)(24+200*intensity),(int)(28+165*intensity),(int)(42+210*intensity)),3);
                }
                caption(c,"2 SLITS",76,85,WHITE,22);caption(c,"SCREEN",765,430,WHITE,20);break;
            }
            case 5:{ // Archimedes: floating fraction depends on density
                box(c,300,95,400,315,Color.rgb(42,64,84));box(c,320,166,360,223,Color.rgb(32,119,164));
                double ratio=y/x;float objY=(float)(ratio<=1?166+80*(1-ratio):320);
                box(c,458,objY,95,98,ORANGE);
                line(c,505,objY+90,505,objY+30,GOLD,5);line(c,505,objY+5,505,objY-60,CYAN,5);
                caption(c,"BUOYANCY",575,220,CYAN,22);caption(c,"WEIGHT",575,330,GOLD,22);
                caption(c,ratio<1?"FLOAT":"SINK",85,190,WHITE,30);break;
            }
            case 6:{ // Faraday sinusoidal flux and emf (quadrature)
                for(int n=0;n<7;n++)ellipse(c,240+n*11,225,36,100,CYAN,3);
                box(c,90,179,70,90,ORANGE);caption(c,"N",110,225,WHITE,25);
                line(c,375,225,940,225,MUTED,2);line(c,420,95,420,345,MUTED,2);
                Path wave=new Path();Path flux=new Path();
                for(int xx=420;xx<=940;xx+=4){
                    float phi=(float)((xx-420)/520.0*2*Math.PI*y);
                    float eyy=(float)(225-110*Math.sin(phi)),byy=(float)(225-95*Math.cos(phi));
                    if(xx==420){wave.moveTo(xx,eyy);flux.moveTo(xx,byy);}
                    else{wave.lineTo(xx,eyy);flux.lineTo(xx,byy);}
                }p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(GOLD);c.drawPath(wave,p);
                p.setColor(CYAN);p.setStrokeWidth(2);c.drawPath(flux,p);p.setStyle(Paint.Style.FILL);
                caption(c,"EMF",640,65,GOLD,24);break;
            }
            case 7:{ // Ohm circuit
                line(c,190,115,790,115,CYAN,6);line(c,790,115,790,330,CYAN,6);
                line(c,790,330,190,330,CYAN,6);line(c,190,330,190,115,CYAN,6);
                box(c,160,178,60,90,GOLD);caption(c,"+",175,208,BG,27);
                box(c,670,92,98,47,ORANGE);caption(c,"R",706,125,BG,26);
                double amp=x/y;float brightness=(float)Math.min(1,amp/1.0);
                strokeCircle(c,480,330,46,WHITE,5);
                circle(c,480,330,12,Color.rgb((int)(80+175*brightness),(int)(72+155*brightness),56));
                caption(c,"I = "+n(amp,2)+" A",377,63,WHITE,26);break;
            }
            case 8:{ // Kepler ellipse with focus star
                float rx=290,ry=(float)(290*Math.sqrt(1-y*y)*.7);
                ellipse(c,500,228,rx,ry,CYAN,4);
                float sun=500-(float)(rx*y);circle(c,sun,228,28,GOLD);circle(c,790,228,12,ORANGE);
                line(c,sun,228,790,228,GOLD,2);caption(c,"SUN",sun-24,185,WHITE,19);
                caption(c,"a = "+n(x,2)+" AU",350,65,WHITE,25);break;
            }
            case 9:{ // Doppler wavefronts compressed ahead for approach
                float sx=(float)(500+Math.sin(t*.5)*40),spacingAhead=(float)(45*(343-y)/343),spacingBack=(float)(45*(343+y)/343);
                circle(c,sx,220,24,ORANGE);
                for(int j=1;j<=5;j++){strokeCircle(c,sx+j*spacingAhead,220,17+j*6,CYAN,3);
                    strokeCircle(c,sx-j*spacingBack,220,17+j*6,GOLD,3);}
                caption(c,"LISTENER",775,95,WHITE,20);
                box(c,820,190,28,70,MUTED);break;
            }
            case 10:{ // pendulum
                float px=500,py=65;double period=2*Math.PI*Math.sqrt(x/y);
                double theta=.34*Math.sin(t*2*Math.PI/period);
                float len=250,xx=px+(float)(len*Math.sin(theta)),yy=py+(float)(len*Math.cos(theta));
                line(c,px,py,xx,yy,CYAN,5);circle(c,xx,yy,28,ORANGE);
                line(c,px,py,px,py+len,MUTED,2);circle(c,px,py,13,GOLD);
                caption(c,"T = "+n(period,2)+" s",65,380,WHITE,26);break;
            }
            case 11:{ // photoelectric ejection threshold
                box(c,555,105,34,235,MUTED);caption(c,"METAL",520,390,WHITE,22);
                for(int k=0;k<4;k++){
                    float yy=140+k*58;line(c,155,yy-25,536,yy,CYAN,5);circle(c,510,yy,12,GOLD);
                }
                double ke=1239.841984/x-y;
                if(ke>0){for(int k=0;k<3;k++){circle(c,650+k*80,148+k*75,11,ORANGE);
                    line(c,600+k*80,148+k*75,650+k*80,148+k*75,ORANGE,3);}}
                else caption(c,"NO ELECTRONS",644,210,ORANGE,25);
                caption(c,"hν = "+n(1239.841984/x,2)+" eV",90,80,GOLD,22);break;
            }
            default:
                if(i>=12)NewLabArt.draw(c,i-12,x,y,u,v,t);
        }
    }
}
