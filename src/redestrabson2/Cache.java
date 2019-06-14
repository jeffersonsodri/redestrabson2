package redestrabson2;

public interface Cache<K, V> {

    /**
     * Pega um value para a {@code key} especifica ou returna {@code null}.
     *
     * @param key key
     * @return o value ou {@code null}.
     */
    V get(K key);

    /**
     * Coloca um value na cache para a {@code key} especificada.
     *
     * @param key   key
     * @param value image
     * @return o valor anterior.
     */
    V put(K key, V value);

    /**
     * Remove a entrada {@code key} se existir, se n�o retorna {@code null}.
     *
     * @return o valor anterior ou @{code null}.
     */
    V remove(K key);

    /**
     * Limpa todas as entradas da Cache.
     */
    void clear();

    /**
     * Retorna o tamanho maximo de mem�ria da cache cache.
     *
     * @return tamanho maximo de mem�ria.
     */
    int getMaxMemorySize();

    /**
     * Retorna do tamanho atual da mem�ria da cache.
     *
     * @return tamanho atual de mem�ria.
     */
    int getMemorySize();

}