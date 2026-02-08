package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.remote.HARDCODED_CARREERID
import me.itzvirtual.myunitox.data.remote.LOCAL_RESPONSES
import me.itzvirtual.myunitox.data.remote.NetworkModule.provideJson
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import javax.inject.Inject


class ExamResultRepository @Inject constructor(private val api: UnitoAPI) {
    suspend fun fetchExamResults(): Result<List<ExamResult>> {
        if (LOCAL_RESPONSES) {
            val ret = """
[{"applistaId":18392047,"cdsId":14928,"adId":62841,"appId":1,"appLogId":1,"stuId":1483920,"adregId":27491053,"adsceId":24718394,"adStuCod":"MFN0582","adStuDes":"PROGRAMMAZIONE I","cdsAdStuCod":"0801L31","statoAdsce":{"value":"F"},"pesoAd":9,"matricola":"1639205","nomeStudente":"MARIO","nomeAlias":"","cognomeStudente":"ROSSI","codFisStudente":"RSSMRA00A01H501U","dataNascitaStudente":"01/01/2000 00:00:00","cdsStuCod":"0801L31","pubblId":583912,"presaVisione":{"value":"A"},"dataRifEsitoStu":"07/02/2026 23:59:59","gruppoVotoCod":"30L","gruppoVotoMaxPunti":30,"esito":{"modValCod":{"value":"V"},"superatoFlg":1,"votoEsa":30,"tipoGiudCod":"","tipoGiudizioDes":"","assenteFlg":0,"ritiratoFlg":0,"laude":1,"acceptEnabled":1,"rejectEnabled":1,"repeatable":1},"dataEsa":"22/01/2026","dataOraTurno":"/Date(1769068800000+0100)/","desAppello":"PROGRAMMAZIONE I","domandeEsame":"","dataIns":"14/01/2026 13:46:25","tipoGestAppCod":"FWA","notaStudente":"","tipoSvolgimentoEsameCod":"","tipoSvolgimentoEsameDes":"","manualeFlg":0,"notaPubbl":"","isAttendanceCertificateEnabled":false}]
            """.trimIndent()
            return Result.success(provideJson().decodeFromString(ret))
        }
        return try {
            val response = api.getExamBoard("$HARDCODED_CARREERID")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}