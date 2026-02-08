package me.itzvirtual.myunitox.data.repository

import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.data.remote.HARDCODED_CARREERID
import me.itzvirtual.myunitox.data.remote.HARDCODED_STUDENTID
import me.itzvirtual.myunitox.data.remote.LOCAL_RESPONSES
import me.itzvirtual.myunitox.data.remote.NetworkModule.provideJson
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import javax.inject.Inject

class ExamDateRepository @Inject constructor(private val api: UnitoAPI) {
    suspend fun fetchExamDates(): Result<List<ExamDate>> {
        if(LOCAL_RESPONSES) {
            val ret = """
[{"adsce_id":24718394,"ad_id":62841,"cod":"MFN0582","title":"PROGRAMMAZIONE I","description":"PROGRAMMAZIONE I","note":"Laboratorio Turing+dijkstra+ V. N","date":"/Date(1770591600000+0100)/","bookingStartDate":"/Date(1768863600000+0100)/","bookingEndDate":"/Date(1769986800000+0100)/","bookableExam":true,"bookingEnabled":false,"unbookingEnabled":false,"cds_app_id":14928,"ad_app_id":62841,"iscritti":319,"posiz":null,"adreg_id":null,"applista_id":null,"app_id":2,"tipo_app_cod":"PF","ad_cod":"MFN0582","ad_des":"PROGRAMMAZIONE I","app_des":"PROGRAMMAZIONE I","presidente":"LUCA MORETTI","tipo_des":null,"stato_quest":1,"des_edificio":null,"des_aula":null,"dom_part_cod":null,"fat_part_cod":null,"turni":null,"sessioni":null,"misureComp":null,"tipiSvolgimentoEsame":null,"chkQuestVal":"0","visCancPren":true},{"adsce_id":29184735,"ad_id":96274,"cod":"INF0348","title":"FONDAMENTI DELL'INFORMATICA","description":"FONDAMENTI DELL'INFORMATICA","note":null,"date":"/Date(1770969600000+0100)/","bookingStartDate":"/Date(1769209200000+0100)/","bookingEndDate":"/Date(1770332400000+0100)/","bookableExam":false,"bookingEnabled":false,"unbookingEnabled":false,"cds_app_id":14928,"iscritti":0,"ad_app_id":96274,"adreg_id":29471836,"applista_id":17392841,"app_id":2,"ad_cod":"INF0348","ad_des":"FONDAMENTI DELL'INFORMATICA","app_des":"FONDAMENTI DELL'INFORMATICA","presidente":null,"tipo_des":"","stato_quest":null,"des_edificio":null,"des_aula":null,"dom_part_cod":null,"fat_part_cod":null,"app_log_id":1,"tipo_app_cod":"PF","nota_studente":"","misureCompensative":[],"tipoSvolgimentoEsameCod":null,"tipoSvolgimentoEsameDes":null,"tipoSvolgimentoEsameRichiestaFlg":null,"des_turno":null,"sesDes":"2025/2026 - SESSIONE UNICA","visCancPren":true},{"adsce_id":28471923,"ad_id":94812,"cod":"INF0326","title":"ARCHITETTURA DEGLI ELABORATORI","description":"ARCHITETTURA DEGLI ELABORATORI","note":"","date":"/Date(1780610400000+0200)/","bookingStartDate":"/Date(1778882400000+0200)/","bookingEndDate":"/Date(1780005600000+0200)/","bookableExam":true,"bookingEnabled":false,"unbookingEnabled":false,"cds_app_id":14928,"ad_app_id":94812,"iscritti":0,"posiz":null,"adreg_id":null,"applista_id":null,"app_id":1,"tipo_app_cod":"PF","ad_cod":"INF0326","ad_des":"ARCHITETTURA DEGLI ELABORATORI","app_des":"ARCHITETTURA DEGLI ELABORATORI","presidente":"STEFANO RINALDI","tipo_des":null,"stato_quest":1,"des_edificio":null,"des_aula":null,"dom_part_cod":null,"fat_part_cod":null,"turni":null,"sessioni":null,"misureComp":null,"tipiSvolgimentoEsame":null,"chkQuestVal":"0","visCancPren":true}]
            """.trimIndent()
            return Result.success(provideJson().decodeFromString(ret))
        }
        return try {
            val response = api.getExamDates("$HARDCODED_STUDENTID", "$HARDCODED_CARREERID")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}