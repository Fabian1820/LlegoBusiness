package com.llego.shared.data.mappers

import com.llego.multiplatform.graphql.CreateProductMutation
import com.llego.multiplatform.graphql.CreateVariantListMutation
import com.llego.multiplatform.graphql.GetBranchApplicableCategoriesQuery
import com.llego.multiplatform.graphql.GetVariantListsQuery
import com.llego.multiplatform.graphql.GetProductCategoriesQuery
import com.llego.multiplatform.graphql.GetProductsByIdsQuery
import com.llego.multiplatform.graphql.GetProductsQuery
import com.llego.multiplatform.graphql.UpdateVariantListMutation
import com.llego.multiplatform.graphql.UpdateProductMutation
import com.llego.shared.data.model.ProductCategory
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.VariantList
import com.llego.shared.data.model.VariantOption

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
        variantListIds = variantListIds,
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
        variantListIds = variantListIds,
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
        variantListIds = variantListIds,
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
        variantListIds = variantListIds,
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

internal fun GetBranchApplicableCategoriesQuery.ApplicableCategory.toDomain(): ProductCategory {
    return ProductCategory(
        id = id,
        branchType = branchType,
        name = name,
        iconIos = iconIos,
        iconWeb = iconWeb,
        iconAndroid = iconAndroid
    )
}

internal fun GetVariantListsQuery.VariantList.toDomain(): VariantList {
    return VariantList(
        id = id,
        branchId = branchId,
        name = name,
        description = description,
        options = options.map { option ->
            VariantOption(
                id = option.id,
                name = option.name,
                priceAdjustment = option.priceAdjustment
            )
        }
    )
}

internal fun CreateVariantListMutation.CreateVariantList.toDomain(): VariantList {
    return VariantList(
        id = id,
        branchId = branchId,
        name = name,
        description = description,
        options = options.map { option ->
            VariantOption(
                id = option.id,
                name = option.name,
                priceAdjustment = option.priceAdjustment
            )
        }
    )
}

internal fun UpdateVariantListMutation.UpdateVariantList.toDomain(): VariantList {
    return VariantList(
        id = id,
        branchId = branchId,
        name = name,
        description = description,
        options = options.map { option ->
            VariantOption(
                id = option.id,
                name = option.name,
                priceAdjustment = option.priceAdjustment
            )
        }
    )
}
