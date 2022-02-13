package ru.tinkoff.invest.openapi.okhttp;

/**
 * Базовый интерфейс для работы с OpenAPI.
 */
public interface Context {

    /**
     * Получение пути URL, в котором ведётся работа.
     *
     * @return Путь в URL.
     */
    String getPath();

}
