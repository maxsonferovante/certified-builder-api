package com.maal.certifiedbuilderapi.business.usecase.certificate.construction;

import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classe para armazenar resultados do processamento paralelo de ordens
 * Thread-safe para operações concorrentes durante processamento paralelo
 */
public class ProcessingResult {
    
    private final List<Integer> existingOrders = new CopyOnWriteArrayList<>();
    private final List<TechOrdersResponse> newOrders = new CopyOnWriteArrayList<>();
    
    /**
     * Adiciona uma ordem que já existe no sistema
     * 
     * @param orderId ID da ordem existente
     */
    public void addExistingOrder(Integer orderId) {
        existingOrders.add(orderId);
    }
    
    /**
     * Adiciona uma nova ordem processada
     * 
     * @param order Nova ordem processada
     */
    public void addNewOrder(TechOrdersResponse order) {
        newOrders.add(order);
    }
    
    /**
     * Retorna lista imutável de ordens existentes
     * 
     * @return Lista de IDs de ordens existentes
     */
    public List<Integer> getExistingOrders() {
        return new ArrayList<>(existingOrders);
    }
    
    /**
     * Retorna lista imutável de novas ordens
     * 
     * @return Lista de novas ordens processadas
     */
    public List<TechOrdersResponse> getNewOrders() {
        return new ArrayList<>(newOrders);
    }
    
    /**
     * Retorna o total de ordens processadas
     * 
     * @return Soma de ordens existentes e novas
     */
    public int getTotalProcessed() {
        return existingOrders.size() + newOrders.size();
    }
    
    /**
     * Verifica se há novas ordens para processar
     * 
     * @return true se existem novas ordens
     */
    public boolean hasNewOrders() {
        return !newOrders.isEmpty();
    }
} 