package jpabook.jpashop.exception;

public class NotEnoughStockException extends RuntimeException {

    // cammand + n 으로 runtime 관련 override method 다 추가해줬음
    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(String message) {
        super(message);
    }

    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }
}
