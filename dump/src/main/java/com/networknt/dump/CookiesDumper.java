package com.networknt.dump;

import com.networknt.mask.Mask;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;

import java.util.*;
/**
 * CookiesDumper is to dump http request/response cookie info to result.
 */
public class CookiesDumper extends AbstractDumper implements IRequestDumpable, IResponseDumpable{
    private Map<String, Object> cookieMap = new LinkedHashMap<>();

    CookiesDumper(DumpConfig config, HttpServerExchange exchange) {
        super(config, exchange);
    }

    /**
     * impl of dumping request cookies to result
     * @param result A map you want to put dump information to
     */
    @Override
    public void dumpRequest(Map<String, Object> result) {
        Map<String, Cookie> cookiesMap = exchange.getRequestCookies();
        dumpCookies(cookiesMap, "requestCookies");
        this.putDumpInfoTo(result);

    }

    /**
     * impl of dumping response cookies to result
     * @param result A map you want to put dump information to
     */
    @Override
    public void dumpResponse(Map<String, Object> result) {
        Map<String, Cookie> cookiesMap = exchange.getResponseCookies();
        dumpCookies(cookiesMap, "responseCookies");
        this.putDumpInfoTo(result);
    }

    /**
     * put cookies info to cookieMap
     * @param cookiesMap Map of cookies
     */
    private void dumpCookies(Map<String, Cookie> cookiesMap, String maskKey) {
        cookiesMap.forEach((key, cookie) -> {
            if(!config.getRequestFilteredCookies().contains(cookie.getName())) {
                List<Map<String, String>> cookieInfoList = new ArrayList<>();
                //mask cookieValue
                String cookieValue = config.isMaskEnabled() ? Mask.maskRegex(cookie.getValue(), maskKey, cookie.getName()) : cookie.getValue();
                cookieInfoList.add(new HashMap<String, String>(){{put(DumpConstants.COOKIE_VALUE, cookieValue);}});
                cookieInfoList.add(new HashMap<String, String>(){{put(DumpConstants.COOKIE_DOMAIN, cookie.getDomain());}});
                cookieInfoList.add(new HashMap<String, String>(){{put(DumpConstants.COOKIE_PATH, cookie.getPath());}});
                cookieInfoList.add(new HashMap<String, String>(){{put(DumpConstants.COOKIE_EXPIRES, cookie.getExpires() == null ? "" : cookie.getExpires().toString());}});
                this.cookieMap.put(key, cookieInfoList);
            }
        });
    }

    /**
     * put cookieMap to result
     * @param result a Map<String, Object> you want to put dumping info to.
     */
    @Override
    protected void putDumpInfoTo(Map<String, Object> result) {
        if(this.cookieMap.size() > 0) {
            result.put(DumpConstants.COOKIES, cookieMap);
        }
    }

    @Override
    public boolean isApplicableForRequest() {
        return config.isRequestCookieEnabled();
    }

    @Override
    public boolean isApplicableForResponse() {
        return config.isResponseCookieEnabled();
    }
}
