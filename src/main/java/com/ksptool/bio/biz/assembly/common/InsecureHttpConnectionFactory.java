package com.ksptool.bio.biz.assembly.common;

import org.eclipse.jgit.transport.http.HttpConnection;
import org.eclipse.jgit.transport.http.JDKHttpConnectionFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.X509Certificate;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
public class InsecureHttpConnectionFactory extends JDKHttpConnectionFactory {

    @Override
    public HttpConnection create(URL url, Proxy proxy) throws IOException {
        //调用父类创建 JGit 的 HttpConnection 对象
        HttpConnection connection = super.create(url, proxy);

        //利用反射动态提取被 JGit 隐藏的真实 HttpURLConnection
        HttpURLConnection wrappedConnection = extractUnderlyingConnection(connection);

        //如果是 HTTPS 请求，强制替换其 SSL 验证逻辑
        if (wrappedConnection instanceof HttpsURLConnection httpsConnection) {
            setupInsecureSsl(httpsConnection);
        }

        return connection;
    }

    /**
     * 顾问特制：反射提取器。它会自动遍历类的所有字段，寻找类型为 HttpURLConnection 的变量。
     */
    private HttpURLConnection extractUnderlyingConnection(HttpConnection connection) {
        Class<?> clazz = connection.getClass();
        //向上遍历类层级，直到找到目标或到达顶级 Object 类
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                //如果发现该字段的类型是 HttpURLConnection（或其子类）
                if (HttpURLConnection.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true); // 穿透 private/protected 的封装限制
                        return (HttpURLConnection) field.get(connection);
                    } catch (Exception e) {
                        // 提取失败，安静忽略，继续尝试下一个
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private void setupInsecureSsl(HttpsURLConnection connection) {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, new java.security.SecureRandom());

            connection.setSSLSocketFactory(context.getSocketFactory());
            connection.setHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            throw new RuntimeException("跳过 SSL 校验失败", e);
        }
    }
}