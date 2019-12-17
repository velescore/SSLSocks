package link.infra.sslsocks;

public class Constants {
	public static final String EXECUTABLE = "stunnel";
	public static final String CONFIG = "config.conf";
	public static final String PSKSECRETS = "psksecrets.txt";
	public static final String PID = "pid";

	public static final String DEF_CONFIG =
			"## Stunel client configuration file\n" +
					"## Part of Veles Network dVPN\n" +
					"\n" +
					"[veles-openvpn]\n" +
					"client = yes\n" +
					"accept = 127.0.0.1:21337\n" +
					"sni = openvpn.vpn.veles.network\n" +
					"connect = <MasternodeIP>:443\n" +
					"\n" +
					"## <MasernodeIP> replace with IP\n" +
					"## of Masternode you are going connect\n" +
					"\n" +
					"foreground = yes\n"+
					"pid = ";
}
