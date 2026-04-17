public class ItemSpeedDown extends Item {
    public ItemSpeedDown(double x, double y) {
        super(x, y, 4); // type 4 代表減速道具
    }

    @Override
    public void applyEffect(Ball targetBall) {
        // 減速效果: 減少球的速度 (例如減少 20%)
        targetBall.velocity.x *= 0.8;
        targetBall.velocity.y *= 0.8;
    }

    
}