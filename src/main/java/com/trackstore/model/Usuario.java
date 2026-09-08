package com.trackstore.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "usuarios")
public class Usuario {

    public enum Tipo { ADMIN, CLIENTE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * CPF com 11 dígitos, sem pontuação, guardado como texto (não número) para não
     * perder zeros à esquerda. Os dígitos verificadores são conferidos em CpfUtil.
     */
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "senha_salt", nullable = false, length = 64)
    private String senhaSalt;

    /** VARCHAR em vez do tipo ENUM do MySQL — veja a nota em Produto.situacao. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Tipo tipo = Tipo.CLIENTE;

    public Usuario() {
    }

    public Usuario(String nome, String email, String cpf, String senhaHash, String senhaSalt, Tipo tipo) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senhaHash = senhaHash;
        this.senhaSalt = senhaSalt;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getSenhaSalt() { return senhaSalt; }
    public void setSenhaSalt(String senhaSalt) { this.senhaSalt = senhaSalt; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public boolean isAdmin() {
        return tipo == Tipo.ADMIN;
    }

    @Override
    public String toString() {
        return nome + " (" + email + ")";
    }
}
