package com.github.bpark.companion.sparql

interface Element {
    fun render(builder: StringBuilder, indent: String)
}

class SPARQL : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("sparql")
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }

    fun select(init: Select.() -> Unit) = Select()
}

class Select : Element{
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("select")
    }

    override fun toString() : String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }

}

fun sparql(init: SPARQL.() -> Unit): SPARQL {
    val sparql = SPARQL()
    sparql.init()
    return sparql
}

fun main(args: Array<String>) {
    val result = sparql { select {  } }
    println(result.toString())
}

