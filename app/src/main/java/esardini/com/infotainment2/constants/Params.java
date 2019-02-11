package esardini.com.infotainment2.constants;

import java.util.Date;

public class Params {

    public static Boolean DEBUG = false; //ricordarsi di disabilitare questa variabile

    //public static final String RASPBERRY = "192.168.43.111";
    //public static final String RASPBERRY = "192.168.43.208"; //macchina
    public static String RASPBERRY = "192.168.2.176"; //VM ufficio
    public static String SOCKET_PORT = "8080";
    public static String SOCKET_ADDRESS = "http://"+ RASPBERRY + ":" + SOCKET_PORT;

    public static String KILL_SERVICE_REQUEST = "KILL_SERVICE_REQUEST";
    public static int PHONE_STATUS_TASK_FREQUENCE = 20000;
    public static int COORDINATES_TASK_FREQUENCE = 1000;
    public static int TRY_CONNECT_TASK_FREQUENCE = 30000; //5 minuti
    public static int TASK_DELAY = 1000;
    public static Boolean KILL_ALL = false;
    public static Date STOP_TIME = null;




    //temporaly not used
    public static final String[] IPLIST = {};
    //public static final String SOCKET_ADDRESS = "http://%s:" + SOCKET_PORT;

}
