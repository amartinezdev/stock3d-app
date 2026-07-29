CREATE TABLE Usuario
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY,
    nombre    VARCHAR(255) NOT NULL,
    email     VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    rol       VARCHAR(255) NOT NULL,
    password  TEXT         NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (email),
    UNIQUE (user_name)
);

CREATE TABLE Producto
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    nombre      VARCHAR(255)   NOT NULL,
    descripcion VARCHAR(255)   NOT NULL,
    precio      NUMERIC(10, 2) NOT NULL,
    categoria   TEXT           NOT NULL,
    peso_rollo  NUMERIC(10, 2) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE en_uso
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id       BIGINT         NOT NULL,
    producto_id      BIGINT         NOT NULL,
    gramos_restantes NUMERIC(10, 2) NOT NULL,
    fecha_apertura   DATE           NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario (id),
    FOREIGN KEY (producto_id) REFERENCES Producto (id)
);

CREATE TABLE inventario
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id  BIGINT  NOT NULL,
    producto_id BIGINT  NOT NULL,
    cantidad    INTEGER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario (id),
    FOREIGN KEY (producto_id) REFERENCES Producto (id),
    UNIQUE (usuario_id, producto_id)
);

CREATE TABLE movimiento
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id  BIGINT         NOT NULL,
    producto_id BIGINT         NOT NULL,
    tipo        TEXT           NOT NULL,
    cantidad    NUMERIC(10, 2) NOT NULL,
    precio      NUMERIC(10, 2),
    fecha       TIMESTAMP      NOT NULL,
    notas       TEXT,
    PRIMARY KEY (id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario (id),
    FOREIGN KEY (producto_id) REFERENCES Producto (id)
);
