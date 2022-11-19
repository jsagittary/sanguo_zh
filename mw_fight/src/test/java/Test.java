import java.util.Arrays;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-18 21:24
 */
public class Test {
    public static void main(String[] args) {
        StudentText[] stu = new StudentText[4];
        stu[0] = new StudentText(101, "lisi");
        stu[1] = new StudentText(100, "zhangsan");
        stu[2] = new StudentText(102, "wangwu");
        stu[3] = new StudentText(101, "xiba");
        System.out.println(Arrays.toString(stu));    //将数组中的各个元素打印出来
        Arrays.sort(stu);   //对数组中元素进行排序
        System.out.println(Arrays.toString(stu));
    }
}

class StudentText implements Comparable<StudentText> {
    private int id;
    private String name;

    public StudentText(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(StudentText o) {
        return this.id > o.id ? -1 : (this.id == o.id ? 0 : 1);
    }

    @Override
    public String toString() {   //重写tostring
        String str = "id:" + id + "name:" + name;
        return str;
    }

}