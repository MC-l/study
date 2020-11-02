package spi.demo;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @auth caiguowei
 * @date 2020/4/17
 */
public class SpiMain {
    public static void main(String[] args) {
        ServiceLoader<Animal> animals = ServiceLoader.load(Animal.class);
        Iterator<Animal> it = animals.iterator();
        while (it.hasNext()){
            Animal animal = it.next();
            animal.sound();
        }

    }
}
