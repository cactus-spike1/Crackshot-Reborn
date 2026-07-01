# CrackShot Reborn API

Русская документация по интеграции CrackShot Reborn с другими Bukkit/Spigot/Paper-плагинами.

## Что использовать извне

Для интеграции есть два основных механизма:

1. `com.shampaggon.crackshot.CSUtility`
Используется, когда вашему плагину нужно вызвать действия CrackShot: выдать оружие, создать `ItemStack`, определить `weaponTitle`, форсировать выстрел, перезарядку и так далее.

2. `com.shampaggon.crackshot.events.*`
Используется, когда ваш плагин должен реагировать на действия оружия или менять их через Bukkit events.

## Подключение в `plugin.yml`

Если ваш плагин не может работать без CrackShot:

```yml
depend: [CrackShot]
```

Если CrackShot нужен только для дополнительного функционала:

```yml
softdepend: [CrackShot]
```

## Получение API

```java
import com.shampaggon.crackshot.CSUtility;

public final class MyPlugin extends JavaPlugin {
    private CSUtility crackShot;

    @Override
    public void onEnable() {
        this.crackShot = new CSUtility();

        if (!this.crackShot.isAvailable()) {
            getLogger().warning("CrackShot не найден, интеграция отключена.");
            return;
        }
    }
}
```

## `CSUtility`: полный список методов

### Информация о плагине

`boolean isAvailable()`
Проверяет, найден ли CrackShot и доступен ли его API.

`String getVersion()`
Возвращает версию CrackShot.

`Plugin getPlugin()`
Возвращает Bukkit-инстанс плагина CrackShot.

`CSDirector getHandle()`
Возвращает основной экземпляр CrackShot. Это низкоуровневый доступ. Используйте только если `CSUtility` не покрывает нужный сценарий.

### Работа со списком оружий

`Collection<String> getWeaponTitles()`
Возвращает все зарегистрированные `weaponTitle` из конфигов.

`String resolveWeaponTitle(String weaponTitle)`
Пытается найти оружие по точному или частичному имени.

`boolean hasWeapon(String weaponTitle)`
Проверяет, существует ли оружие.

### Создание и выдача оружия

`boolean giveWeapon(Player receiver, String weaponTitle, int amount)`
Выдаёт игроку оружие через внутреннюю логику CrackShot.

`ItemStack generateWeapon(String weaponTitle)`
Создаёт `ItemStack` оружия без выдачи игроку.

`ItemStack generateWeapon(String weaponTitle, int amount)`
Создаёт `ItemStack` оружия и задаёт количество.

### Определение оружия

`boolean isWeapon(ItemStack item)`
Проверяет, является ли предмет оружием CrackShot.

`String getWeaponTitle(ItemStack item)`
Пытается определить `weaponTitle` по предмету.

`String getWeaponTitle(Player player)`
Пытается определить `weaponTitle` по предмету в руке игрока.

`String getWeaponTitle(Projectile projectile)`
Читает `weaponTitle` из метадаты снаряда CrackShot.

`String getWeaponTitle(TNTPrimed tnt)`
Читает `weaponTitle` из метадаты TNT, созданного CrackShot.

### Метаданные и параметры оружия

`Integer getCustomModelData(String weaponTitle)`
Возвращает `Custom_Model_Data` для оружия. Поддерживаются ключи:

- `Item_Information.Custom_Model_Data`
- `Item_Information.CustomModelData`
- `Item_Information.CMD`

`int getReloadAmount(Player player, String weaponTitle, ItemStack item)`
Возвращает текущую ёмкость магазина. Внутри вызывает `WeaponCapacityEvent`, поэтому сторонние плагины тоже могут влиять на результат.

`String[] getAttachment(String weaponTitle, ItemStack item)`
Возвращает массив:

- индекс `0`: тип attachment
- индекс `1`: активный attachment

`boolean isDualWield(Player player, String weaponTitle, ItemStack item)`
Проверяет, считается ли оружие dual wield для данного игрока и предмета.

### Управление поведением оружия

`boolean shootHeldWeapon(Player player, boolean leftClick)`
Принудительно запускает выстрел из оружия, которое игрок держит в руке.

`boolean reloadHeldWeapon(Player player)`
Принудительно запускает перезарядку оружия, которое игрок держит в руке.

`void unscope(Player player)`
Принудительно снимает режим прицеливания.

### Взрывы, мины и снаряды

`void generateExplosion(Player player, Location loc, String weaponTitle)`
Запускает штатную логику взрыва CrackShot в указанной точке.

`void spawnMine(Player player, Location loc, String weaponTitle)`
Размещает мину или ловушку через внутреннюю механику CrackShot.

`void setProjectile(Player player, Projectile proj, String weaponTitle)`
Привязывает к существующему снаряду владельца и `weaponTitle`, чтобы CrackShot и другие плагины могли распознавать этот снаряд как CrackShot-снаряд.

Поддерживаемые типы снарядов:

- `ARROW`
- `SNOWBALL`
- `FIREBALL`
- `WITHER_SKULL`
- `EGG`

## Примеры использования

### Выдать игроку оружие

```java
CSUtility api = new CSUtility();
api.giveWeapon(player, "AK-47", 1);
```

### Создать предмет и положить в инвентарь вручную

```java
CSUtility api = new CSUtility();
ItemStack weapon = api.generateWeapon("Bazooka", 1);

if (weapon != null) {
    player.getInventory().addItem(weapon);
}
```

### Проверить, что игрок держит оружие CrackShot

```java
CSUtility api = new CSUtility();
String weaponTitle = api.getWeaponTitle(player);

if (weaponTitle != null) {
    player.sendMessage("У тебя в руке: " + weaponTitle);
}
```

### Форсировать выстрел

```java
CSUtility api = new CSUtility();
boolean fired = api.shootHeldWeapon(player, false);
```

`false` означает правый клик, `true` означает левый клик.

### Форсировать перезарядку

```java
CSUtility api = new CSUtility();
api.reloadHeldWeapon(player);
```

### Привязать свой снаряд к оружию CrackShot

```java
CSUtility api = new CSUtility();
Snowball snowball = player.launchProjectile(Snowball.class);
api.setProjectile(player, snowball, "AK-47");
```

После этого в `ProjectileHitEvent` или других местах можно получить `weaponTitle`:

```java
String weaponTitle = api.getWeaponTitle(snowball);
```

## События Bukkit

Все события лежат в пакете:

```java
com.shampaggon.crackshot.events
```

### Выстрел и подготовка к нему

`WeaponPrepareShootEvent`
Самое раннее событие перед выстрелом. Можно отменить.

`WeaponPreShootEvent`
Срабатывает перед выстрелом. Можно:

- отменить выстрел
- поменять звуки
- поменять разброс

`WeaponShootEvent`
Срабатывает после создания и запуска снаряда.

### Урон и попадания

`WeaponDamageEntityEvent`
Срабатывает при нанесении урона сущности. Можно:

- отменить урон
- изменить урон через `setDamage`

Также содержит флаги:

- `isHeadshot()`
- `isBackstab()`
- `isCritical()`

`WeaponHitBlockEvent`
Срабатывает при попадании снаряда в блок.

### Перезарядка

`WeaponReloadEvent`
Срабатывает при старте перезарядки. Можно менять:

- звуки
- длительность
- скорость

`WeaponReloadCompleteEvent`
Срабатывает после завершения перезарядки.

`WeaponCapacityEvent`
Срабатывает при расчёте ёмкости магазина. Можно менять ёмкость через `setCapacity`.

### Прицеливание

`WeaponScopeEvent`
Срабатывает при входе и выходе из режима прицеливания. Можно отменить.

### Attachments и dual wield

`WeaponAttachmentEvent`
Срабатывает при расчёте attachment. Можно изменить attachment или отменить его применение.

`WeaponAttachmentToggleEvent`
Срабатывает при переключении attachment-режима. Можно отменить или изменить задержку переключения.

`WeaponDualWieldEvent`
Позволяет изменить решение о том, считается ли оружие dual wield.

### Firearm action

`WeaponFirearmActionEvent`
Позволяет менять скорость механики затвора, помпы, рычага и похожих действий.

### Взрывы и ловушки

`WeaponExplodeEvent`
Срабатывает при взрыве от оружия, гранаты или airstrike.

`WeaponPlaceMineEvent`
Срабатывает при установке мины или ловушки.

`WeaponTriggerEvent`
Срабатывает при активации ловушки или триггерного устройства. Можно отменить.

## Примеры событий

### Изменить урон конкретного оружия

```java
@EventHandler
public void onDamage(WeaponDamageEntityEvent event) {
    if (!event.getWeaponTitle().equalsIgnoreCase("Deagle")) {
        return;
    }

    event.setDamage(event.getDamage() * 1.5);
}
```

### Отменить прицеливание для игроков без права

```java
@EventHandler
public void onScope(WeaponScopeEvent event) {
    if (!event.getPlayer().hasPermission("myplugin.scope")) {
        event.setCancelled(true);
    }
}
```

### Увеличить магазин для VIP-игроков

```java
@EventHandler
public void onCapacity(WeaponCapacityEvent event) {
    if (event.getPlayer().hasPermission("myplugin.vip")) {
        event.setCapacity(event.getCapacity() + 10);
    }
}
```

### Ускорить перезарядку

```java
@EventHandler
public void onReload(WeaponReloadEvent event) {
    if (event.getWeaponTitle().equalsIgnoreCase("AK-47")) {
        event.setReloadSpeed(1.5D);
    }
}
```

## Практические рекомендации

- Используйте `CSUtility` как основной внешний API.
- Не вызывайте внутренние методы `CSDirector` напрямую без необходимости.
- Если вы вмешиваетесь в механику через события, держите логику быстрой: обработчики вызываются в основном потоке сервера.
- Если вам нужно только узнать, что сделал CrackShot, используйте events.
- Если вам нужно инициировать действие CrackShot из своего плагина, используйте `CSUtility`.

## Ограничения

- `shootHeldWeapon` и `reloadHeldWeapon` работают с предметом, который игрок держит в руке в момент вызова.
- `setProjectile` не делает любой снаряд полноценным CrackShot-оружием сам по себе; он лишь привязывает владельца и `weaponTitle` к поддерживаемым типам projectile.
- `getHandle()` даёт доступ к внутренностям плагина. Это удобно, но менее стабильно для долгосрочной интеграции, чем `CSUtility` и events.
