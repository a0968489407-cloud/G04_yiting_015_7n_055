public class ItemLife extends Item {
    public ItemLife(double x, double y) {
        super(x, y, 6); // type 6 為生命道具
    }

    @Override
    public void applyEffect(Ball targetBall) {
        targetBall.lives++; // 增加生命
    }
}