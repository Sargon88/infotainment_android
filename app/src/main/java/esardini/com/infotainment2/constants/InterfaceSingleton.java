package esardini.com.infotainment2.constants;

import android.support.design.widget.FloatingActionButton;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

//singleton class
public class InterfaceSingleton {

    private static InterfaceSingleton interface_singleton = null;

    private TextView debug;

    private FloatingActionButton startbtn;
    private FloatingActionButton homebtn;
    private FloatingActionButton youtubebtn;
    private FloatingActionButton wazebtn;
    private Button yt_playbtn;
    private Button yt_pausebtn;
    private Button  yt_stopbtn;
    private ProgressBar waitingBar;


    private InterfaceSingleton(){}

    public static InterfaceSingleton getInstance(){
        if(interface_singleton == null){
            interface_singleton = new InterfaceSingleton();
        }

        return interface_singleton;
    }

    /* GETTER - SETTER */
    public FloatingActionButton getStartbtn() {
        return startbtn;
    }

    public void setStartbtn(FloatingActionButton startbtn) {
        this.startbtn = startbtn;
    }

    public FloatingActionButton getHomebtn() {
        return homebtn;
    }

    public void setHomebtn(FloatingActionButton homebtn) {
        this.homebtn = homebtn;
    }

    public FloatingActionButton getYoutubebtn() {
        return youtubebtn;
    }

    public void setYoutubebtn(FloatingActionButton youtubebtn) {
        this.youtubebtn = youtubebtn;
    }

    public FloatingActionButton getWazebtn() {
        return wazebtn;
    }

    public void setWazebtn(FloatingActionButton wazebtn) {
        this.wazebtn = wazebtn;
    }

    public Button getYt_playbtn() {
        return yt_playbtn;
    }

    public void setYt_playbtn(Button yt_playbtn) {
        this.yt_playbtn = yt_playbtn;
    }

    public Button getYt_pausebtn() {
        return yt_pausebtn;
    }

    public void setYt_pausebtn(Button yt_pausebtn) {
        this.yt_pausebtn = yt_pausebtn;
    }

    public Button getYt_stopbtn() {
        return yt_stopbtn;
    }

    public void setYt_stopbtn(Button yt_stopbtn) {
        this.yt_stopbtn = yt_stopbtn;
    }

    public ProgressBar getWaitingBar() {
        return waitingBar;
    }

    public void setWaitingBar(ProgressBar waitingBar) {
        this.waitingBar = waitingBar;
    }
}
