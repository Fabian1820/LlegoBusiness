package com.llego.shared.data.mappers

import com.llego.multiplatform.graphql.CreateProductMutation
import com.llego.multiplatform.graphql.GetProductCategoriesQuery
import com.llego.multiplatform.graphql.GetProductsByIdsQuery
import com.llego.multiplatform.graphql.GetProductsQuery
import com.llego.multiplatform.graphql.UpdateProductMutation
import com.llego.shared.data.model.ProductCategory
import com.llego.shared.data.model.Product

/**
 * Mappers de productos para convertir GraphQL a dominio.
 */

/**
 * Mapea un ProductType de GraphQL (estructura paginada) a un modelo de dominio Product
 *
 * Nota: Los campos weight, currency e imageUrl son no-nullable en el schema GraphQL
 * y por lo tanto siempre tienen valor. El mapeo directo garantiza la consistencia de tipos.
 */
internal fun GetProductsQuery.Node.toDomain(): Product {
    return Product(
        id = id,
        branchId = branchId,
        name = name,
        description = description,
        weight = weight,              // String! -> String (no nullable)
        price = price,
        currency = currency,          // String! -> String (no nullable)
        image = image,
        availability = availability,
        categoryId = categoryId,
        createdAt = createdAt.toString(),
        imageUrl = imageUrl           // String! -> String (no nullable)
    )
}

/**
 * Mapea un ProductType de GraphQL a un modelo de dominio Product (para query de IDs)
 *
 * Nota: Los campos weight, currency e imageUrl son no-nullable en el schema GraphQL
 * y por lo tanto siempre tienen valor. El mapeo directo garantiza la consistencia de tipos.
 */
internal fun GetProductsByIdsQuery.Node.toDomain(): Product {
    return Product(
        id = id,
        branchId = branchId,
        name = name,
        description = description,
        weight = weight,              // String! -> String (no nullable)
        price = price,
        currency = currency,          // String! -> String (no nullable)
        image = image,
        availability = availability,
        categoryId = categoryId,
        createdAt = createdAt.toString(),
        imageUrl = imageUrl           // String! -> String (no nullable)
    )
}

/**
 * Mapea CreateProductMutation.CreateProduct a modelo de dominio
 *
 * Nota: Los campos weight, currency e imageUrl son no-nullable en el schema GraphQL
 * y por lo tanto siempre tienen valor. El mapeo directo garantiza la consistencia de tipos.
 */
internal fun CreateProductMutation.CreateProduct.toDomain(): Product {
    return Product(
        id = id,
        branchId = branchId,
        name = name,
        description = description,
        weight = weight,              // String! -> String (no nullable)
        price = price,
        currency = currency,          // String! -> String (no nullable)
        image = image,
        availability = availability,
        categoryId = categoryId,
        createdAt = createdAt.toString(),
        imageUrl = imageUrl           // String! -> String (no nullable)
    )
}

/**
 * Mapea UpdateProductMutation.UpdateProduct a modelo de dominio
 *
 * Nota: Los campos weight, currency e imageUrl son no-nullable en el schema GraphQL
 * y por lo tanto siempre tienen valor. El mapeo directo garantiza la consistencia de tipos.
 */
internal fun UpdateProductMutation.UpdateProduct.toDomain(): Product {
    return Product(
        id = id,
        branchId = branchId,
        name = name,
        description = description,
        weight = weight,              // String! -> String (no nullable)
        price = price,
        currency = currency,          // String! -> String (no nullable)
        image = image,
        availability = availability,
        categoryId = categoryId,
        createdAt = createdAt.toString(),
        imageUrl = imageUrl           // String! -> String (no nullable)
    )
}

internal fun GetProductCategoriesQuery.ProductCategory.toDomain(): ProductCategory {
    return ProductCategory(
        id = id,
        branchType = branchType,
        name = name,
        iconIos = iconIos,
        iconWeb = iconWeb,
        iconAndroid = iconAndroid
    )
}
