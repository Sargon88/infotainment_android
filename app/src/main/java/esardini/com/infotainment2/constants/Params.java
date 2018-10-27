package esardini.com.infotainment2.constants;

public class Params {

    public static final Boolean DEBUG = false; //ricordarsi di disabilitare questa variabile

    //public static final String RASPBERRY = "192.168.43.111";
    public static final String RASPBERRY = "192.168.43.208"; //macchina
    //public static final String RASPBERRY = "192.168.43.209"; //ufficio
    public static final String SOCKET_PORT = "8080";
    public static final String SOCKET_ADDRESS = "http://"+ RASPBERRY + ":" + SOCKET_PORT;

    public static final String KILL_SERVICE_REQUEST = "KILL_SERVICE_REQUEST";
    public static final int PHONE_STATUS_TASK_FREQUENCE = 20000;
    public static final int COORDINATES_TASK_FREQUENCE = 1000;
    public static final int TRY_CONNECT_TASK_FREQUENCE = 30000;
    public static final int TASK_DELAY = 1000;





    //temporaly not used
    public static final String[] IPLIST = {"192.168.43.111",
            "192.168.43.208",
            "192.168.43.207"};
    //public static final String SOCKET_ADDRESS = "http://%s:" + SOCKET_PORT;

}
