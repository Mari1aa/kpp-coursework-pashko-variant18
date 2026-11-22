package ua.edu.nupp.kpp.client;

/**
 * Сервер недоступний (немає з'єднання або вичерпано час очікування).
 * Відрізняється від звичайної відмови сервера (код 400/500), після якої
 * можна продовжувати обробку наступних рядків.
 */
public class ServerUnavailableException extends RuntimeException {

    public ServerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
