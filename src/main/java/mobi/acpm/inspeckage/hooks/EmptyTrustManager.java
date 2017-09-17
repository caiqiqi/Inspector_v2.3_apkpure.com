package mobi.acpm.inspeckage.hooks;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/* compiled from: SSLPinningHook */
class EmptyTrustManager implements X509TrustManager {
    private static TrustManager[] emptyTM = null;

    EmptyTrustManager() {
    }

    public static TrustManager[] getInstance() {
        if (emptyTM == null) {
            emptyTM = new TrustManager[1];
            emptyTM[0] = new EmptyTrustManager();
        }
        return emptyTM;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
