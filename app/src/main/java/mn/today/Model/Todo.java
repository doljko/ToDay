package mn.today.Model;

/**
 * Created by Doljko on 1/27/2017.
 */

public class Todo {
    private int todoId;
    private String todoTitle;
    private String todoDescription;
    private String todoStart;
    private String todoEnd;
    public Todo(){

    }
    public Todo(int _todoId,String _todoTitle,String _todoDescription,String _todoStart, String _todoEnd){
        setTodoId(_todoId);
        setTodoTitle(_todoTitle);
        setTodoDescription(_todoDescription);
        setTodoStart(_todoStart);
        setTodoEnd(_todoEnd);
    }

    public Todo(String _todoTitle, String _todoDescription, String _todoStart, String _todoEnd) {
        setTodoTitle(_todoTitle);
        setTodoDescription(_todoDescription);
        setTodoStart(_todoStart);
        setTodoEnd(_todoEnd);
    }

    public int getTodoId() {
        return todoId;
    }

    public void setTodoId(int todoId) {
        this.todoId = todoId;
    }

    public String getTodoTitle() {
        return todoTitle;
    }

    public void setTodoTitle(String todoTitle) {
        this.todoTitle = todoTitle;
    }

    public String getTodoDescription() {
        return todoDescription;
    }

    public void setTodoDescription(String todoDescription) {
        this.todoDescription = todoDescription;
    }

    public String getTodoStart() {
        return todoStart;
    }

    public void setTodoStart(String todoStart) {
        this.todoStart = todoStart;
    }

    public String getTodoEnd() {
        return todoEnd;
    }

    public void setTodoEnd(String todoEnd) {
        this.todoEnd = todoEnd;
    }
}
