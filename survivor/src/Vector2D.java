public class Vector2D {
    // 儲存 X 軸與 Y 軸的浮點數數值
    public double x, y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // 向量加法，將另一個向量的 x, y 值加到自己的 x, y 上
    public void add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
    }
}