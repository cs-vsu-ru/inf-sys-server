package vsu.cs.is.infsysserver.employee;

public enum LessonsOperation {
    CREATE_EMPTY(""),
    DELETE("delete_lessons/");

    private final String urlPart;

    LessonsOperation(String urlPart) {
        this.urlPart = urlPart;
    }

    public String getUrlPart() {
        return urlPart;
    }
}
