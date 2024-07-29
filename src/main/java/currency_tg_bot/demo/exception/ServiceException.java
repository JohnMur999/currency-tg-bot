package currency_tg_bot.demo.exception;

public class ServiceException extends Exception {

    private int errorCode;
    private String errorMessage;

    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
    }

    public ServiceException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public ServiceException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ServiceException{" +
                "errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
