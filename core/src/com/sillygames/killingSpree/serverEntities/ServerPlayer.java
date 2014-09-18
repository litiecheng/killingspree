package com.sillygames.killingSpree.serverEntities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.sillygames.killingSpree.helpers.WorldBodyUtils;
import com.sillygames.killingSpree.helpers.EntityUtils.ActorType;
import com.sillygames.killingSpree.helpers.Utils;
import com.sillygames.killingSpree.managers.physics.Body.BodyType;
import com.sillygames.killingSpree.networking.messages.ControlsMessage;
import com.sillygames.killingSpree.networking.messages.EntityState;

public class ServerPlayer extends ServerEntity{

    public static final float WIDTH = 20;
    public static final float HEIGHT = 20;    
    private ControlsMessage currentControls;
    private boolean markForDispose;
    private float reloadTime;
    
    public ServerPlayer(short id, float x, float y, WorldBodyUtils world) {
        super(id, x, y, world);
        markForDispose = false;
        currentControls = new ControlsMessage();
        actorType = ActorType.PLAYER;
        body = world.addBox(WIDTH - 3f, HEIGHT, x, y,
                BodyType.DynamicBody);
        body.setUserData(this);
        reloadTime = 0;
    }
    
    @Override
    public void update(float delta) {
        if (markForDispose) {
            dispose();
            return;
        }
        processPlayer();
        position.set(body.getPosition());
        reloadTime += delta;
    }
    
    public void markForDispose() {
        markForDispose = true;
    }
    
    private void processPlayer() {
        processControls(currentControls);
    }

    public void processControls(ControlsMessage controls) {
        Vector2 velocity = body.getLinearVelocity();
        Vector2 position = body.getPosition();

        if (velocity.y < -200f) {
            velocity.y = -200f;
        }
        
        if(Math.abs(velocity.x) < 100f) {
            if (controls.right()){
                velocity.x = 100f;
            } else if (controls.left()){
                velocity.x = -100f;
            }
        }
        if(!controls.right() && !controls.left()) {
            velocity.x += -0.1f * velocity.x;
        }
        
        body.setLinearVelocity(velocity);

        if (Utils.wrapBody(position)) {
            body.setTransform(position, 0);
        }

        if (controls.jump() && velocity.y == 0) {
            body.applyLinearImpulse(0, 290f);
        }
        
        float x = controls.right()? 1 : (controls.left()? -1 : 0);
        float y = controls.up()? 1 : (controls.down()? -1 : 0);
        if (x==0 && y==0) {
            x=1;
        }
        if (x ==1 && y == 1) {
           x = 0.707f;
           y = 0.707f;
        }
        if (controls.shoot() && reloadTime > 1) {
            world.AddArrow(position.x + x * 10, position.y + y * 10).body.
            setLinearVelocity(x * 150, y * 150);;
            reloadTime = 0;
        }
    }

    public void setCurrentControls(ControlsMessage currentControls) {
        this.currentControls = currentControls;
    }

    @Override
    public void dispose() {
        world.destroyBody(body);
    }

    @Override
    public void updateState(EntityState state) {
        super.updateState(state);
    }
    
}
