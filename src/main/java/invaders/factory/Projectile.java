package invaders.factory;

import invaders.entities.Player;
import invaders.gameobject.GameObject;
import invaders.memento.Memento;
import invaders.physics.Collider;
import invaders.physics.Vector2D;
import invaders.rendering.Renderable;
import javafx.scene.image.Image;

public abstract class Projectile implements Renderable, GameObject {
    private int lives = 1;
    private Vector2D position;
    private Image image;

    public Projectile(Vector2D position, Image image) {
        this.position = position;
        this.image = image;
    }

    @Override
    public Vector2D getPosition() {
        return position;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public Layer getLayer() {
        return Layer.FOREGROUND;
    }

    @Override
    public void start() {}

    @Override
    public double getWidth() {
        return 10;
    }

    @Override
    public double getHeight() {
        return 10;
    }

    @Override
    public void takeDamage(double amount) {
        this.lives-=1;
    }

    @Override
    public double getHealth() {
        return this.lives;
    }

    @Override
    public boolean isAlive() {
        return this.lives>0;
    }

    private static class ProjectileMemento implements Memento {
        public final Vector2D position;
        public final int lives;
        private final Image image;
        ProjectileMemento(Vector2D position, int lives, Image image) {
            this.position = new Vector2D(position.getX(), position.getY());
            this.lives = lives;
            this.image = image;
        }
    }
    @Override
    public Memento save() { return new ProjectileMemento(position, lives, image); }

    @Override
    public void restore(Memento memento) {
        ProjectileMemento projectileMemento = (ProjectileMemento) memento;
        this.position.setX(projectileMemento.position.getX());
        this.position.setY(projectileMemento.position.getY());
        this.lives = projectileMemento.lives;
        this.image = projectileMemento.image;
    }
}
