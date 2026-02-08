package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.DashboardDataResponse
import me.itzvirtual.myunitox.data.remote.HARDCODED_CARREERID
import me.itzvirtual.myunitox.data.remote.HARDCODED_STUDENTID
import me.itzvirtual.myunitox.data.remote.LOCAL_RESPONSES
import me.itzvirtual.myunitox.data.remote.NetworkModule.provideJson
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import javax.inject.Inject

class DashboardDataRepository @Inject constructor(private val api: UnitoAPI) {
    suspend fun fetchDashboardData(): Result<DashboardDataResponse> {
        if (LOCAL_RESPONSES) {
            val ret = """
            {"grades":[{"Language":"it","IdStudent":1483920,"id":null,"name":"ALTRE ATTIVITA'","description":"29.000000","date":"/Date(1738018800000+0100)/","value":29.0,"average":29.0,"position":1},{"Language":"it","IdStudent":1483920,"id":null,"name":"ALTRE ATTIVITA'","description":"18.000000","date":"/Date(1749420000000+0200)/","value":18.0,"average":23.5,"position":2},{"Language":"it","IdStudent":1483920,"id":null,"name":"MATEMATICA DISCRETA, ALGEBRA E GEOMETRIA","description":"25.000000","date":"/Date(1768518000000+0100)/","value":25.0,"average":24.0,"position":3}],"career":{"Language":"ita","id":null,"description":null,"total":180.0,"registered":24.0,"calculated":24.0,"idStudent":1483920},"stats":{"PersId":null,"Matricola":null,"TipoMediaCod":"P","AdPianif":0,"CfuAdPianif":0,"CfuPiano":66,"BaseVotoAritmetica":88.917,"BaseVotoPonderata":88.917,"MaxPuntiCds":110,"MaxPuntiEsa":30,"id":null,"num_exams":4,"arithmetic_avg":24.25,"weight_avg":24.25,"cfu_gained":24.0,"num_completed_activities":4,"num_remaining_activities":8,"cfu_tot":180,"idStudent":1483920,"umPesoCod":"C","medie":[{"base":30,"definizione":"CDSORD","tipo":"A","value":24.0},{"base":30,"definizione":"CDSORD","tipo":"P","value":24.25}],"cfuCalculated":24.0,"umPesoDes":"Cfu","cfu_progression":13.333333}}
            """.trimIndent()
            return Result.success(provideJson().decodeFromString(ret))
        }
        return try {
            val response = api.getDashboardData(
                "$HARDCODED_STUDENTID", "$HARDCODED_CARREERID"
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}