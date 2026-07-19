package com.alvaromartinez.stock_api.model;

/**
 * Tipos de filamento disponibles. Enum en vez de String libre: así es
 * imposible guardar una categoría que no esté en esta lista. Producto la
 * persiste como texto con @Enumerated(EnumType.STRING).
 */
public enum Categoria {
    PLA,
    PLA_PLUS,
    PETG,
    ABS,
    ASA,
    TPU,
    NYLON,
    PC,
    HIPS,
    PVA,
    BVOH,
    PP,
    PEEK,
    PEI,
    PMMA,
    WOOD,
    CARBON_FIBER,
    GLASS_FIBER,
    SILK,
    MARBLE,
    METAL,
    GLOW_IN_THE_DARK,
    CONDUCTIVE
}