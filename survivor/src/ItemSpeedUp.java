public class ItemSpeedUp extends Item {
    public ItemSpeedUp(double x, double y) {
        super(x, y, 2); // type 2 代表加速道具
    }

    @Override
    public void applyEffect(Ball targetBall) {
        // 加速效果: 增加球的速度 (例如增加 20%)
        targetBall.velocity.x *= 1.2;
        targetBall.velocity.y *= 1.2;
    }
}