package dev.hazar.hazarsheadlibrary.data


fun List<HeadData>.findSingleByName(name: String): HeadData? {
    return this.firstOrNull { it.name.equals(name, ignoreCase = true) }
}

fun List<HeadData>.filterByName(query: String): List<HeadData> {
    return this.filter { it.name.contains(query, ignoreCase = true) }
}

fun List<HeadData>.filterByCategory(category: String): List<HeadData> {
    return this.filter { it.category.equals(category, ignoreCase = true) }
}

fun List<HeadData>.filterByType(type: HeadType): List<HeadData> {
    return this.filter { it.type == type }
}