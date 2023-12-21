package com.example.batiklens

class DataClass {

    var dataTitle: String? = null
    var dataDesc: String? = null
    var dataImage: String? = null

    constructor(dataTitle: String?, dataDesc: String?,  dataImage: String?){
        this.dataTitle = dataTitle
        this.dataDesc = dataDesc
        this.dataImage = dataImage
    }
    constructor()
    {}
}