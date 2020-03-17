package cn.laoshini.dk.console.util;

import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.domain.responese.Result;

/**
 * @author fagarine
 */
public class HttpUtil {
    private HttpUtil() {
    }

    public static <T> T requestEntity(String url, HttpMethod method, Object requestBody, Class<T> responseEntityType,
            HttpStatus expectStatus, Object... urlVariables) {
        if (expectStatus == null) {
            expectStatus = HttpStatus.OK;
        }

        RestTemplate template = new RestTemplate();
        RequestCallback body = template.httpEntityCallback(requestBody);
        ResponseExtractor<ResponseEntity<T>> response = template.responseEntityExtractor(responseEntityType);
        ResponseEntity<T> responseEntity = template.execute(url, method, body, response);
        if (responseEntity != null && expectStatus.equals(responseEntity.getStatusCode())) {
            return responseEntity.getBody();
        }
        return null;
    }

    public static <M> RespMessage<M> gmRequest(String gmUrl, ReqMessage<?> msg) {
        RespMessage<M> message = requestEntity(gmUrl, HttpMethod.POST, msg, RespMessage.class, HttpStatus.OK);
        return message == null ? RespMessage.noResponse() : message;
    }

    public static <T> Result<T> requestResult(String url, HttpMethod method, Map<String, Object> requestBody,
            HttpStatus expectStatus) {
        Result<T> result = requestEntity(url, method, requestBody, Result.class, expectStatus);
        return result == null ? Result.fail() : result;
    }

    public static <T> Result<T> requestResult(String url, HttpMethod method, Map<String, Object> requestBody) {
        return requestResult(url, method, requestBody, HttpStatus.OK);
    }
}
