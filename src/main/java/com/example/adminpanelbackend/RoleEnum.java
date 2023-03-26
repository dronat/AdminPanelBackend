package com.example.adminpanelbackend;

public enum RoleEnum {
    ROLES_MANAGEMENT("Roles Management", "Добавление/удаление/редактирование/просмотр ролей"),
    ADMINS_MANAGEMENT("Admins Management", "Добавление/удаление/редактирование/просмотр админов, а так же загрузка файла с картами в бд"),
    ROTATION_MANAGEMENT("Rotation Management", "Добавление/удаление/редактирование/просмотр ротации"),
    RULES_MANAGEMENT("Rules Management", "Добавление/удаление/редактирование/просмотр правил"),
    ADMIN_LOG("Admin log access", "Просмотр лога действий администратора"),
    BASE("Base access", "Доступ к списку игроков, добавлению новых игроков, " +
            "карточке игрока и всем action'ам в ней, смене текущей/следующей карты, информация " +
            "о сервере на главном экране, логу чата игроков и банов");

    public final String name;
    public final String description;

    RoleEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}