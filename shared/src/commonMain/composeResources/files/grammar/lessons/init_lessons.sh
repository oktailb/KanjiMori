#!/bin/bash

# cat ../grammar.json  | grep '"id"' | cut -d '"' -f 4 | sed 's/$/.html/g' | sed 's/^/echo "<html>\n\t<head><\/head>\n\t<body>\n\t<\/body>\n<\/html>" > /g' > init_lessons.sh

echo "<html>
	<head></head>
	<body>
	</body>
</html>" > verb_dict_form.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_verbale.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_ta.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_te.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > conjugaison_conditionnel.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > adjectifs_i.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > adjectifs_na.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > noms.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > noms_temps.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > noms_lieux.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particules_de_base.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > aru_iru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ato_de.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_dasu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_hajimeru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_ni_iku_kuru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_sugiru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > chu_ju.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_de.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > desho_daro.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_conjonctive_i.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_masu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_naide_kudasai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_dict.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_tai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_ga.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_ha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ha_ikemasen.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ho_ga_ii.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kara_made.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kara_node.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koto_ga_dekiru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mae_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_mo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mo_ii_desu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > superlatif.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mo_mada.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nado.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nakereba_narimasen.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > naru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_ne.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_no.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > no_desu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_to.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_iu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_omoimasu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > tsumori_desu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ta_koto_ga_aru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ta_ri_shimasu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_iru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_kara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_wo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_ya.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_yo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > particule_yori.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > yori_no_ho_ga.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > amari_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_au.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_nagara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_so.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_tsuzukeru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_yasui_nikui.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > beki.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > conditionnel_naraba.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > conditionnel_to.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > conditionnel_eba.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > conditionnel_tara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > dake_shika_nomi.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > conjecturale_to_suru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_areru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_aseru_saseru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_eru_rareru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_nasai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_saserareru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_zuni_naide.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > forme_conjecturale.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > garu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > hazu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > hoshii.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ka_do_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > keigo_bases.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ki_ni_suru_naru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koto_ni_suru_naru_kimeru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_yotte_yoruto_yoreba.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > noni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > rashii.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > shi.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > suru_adjectif.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > toki_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ta_bakari.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ta_ra_do_desu_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_ageru_sashiageru_yaru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_aru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_bakari_iru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_itadakemasen_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_kuremasen_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_kureru_kudasaru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_miru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_mo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_morau_itadaku.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_oku.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_shimau.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_sumimasen.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_yokatta.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > yo_ni_suru_naru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_gachi.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_kireru_kirenai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_kkonai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_shidai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_yo_ga_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > darake.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > dokoro_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ha_motoyori_mochiron.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ippo_da.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kara_ni_kakete.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kawari_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koso.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kuseni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mono_n3.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nagara_mo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kagitte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kan_shite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kawatte_kawari.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kimatte_iru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kurabete.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kuwaete.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_taishite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_totte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_tsuke_tsukete_tsuitemo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > okage_de.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > sae_eb.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > seide.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > tabi_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > tatoe_te_mo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_ieba.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_ittara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_iu_to.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > tokoro_he_ni_wo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > toori_ni_doori_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > toshite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > tsuide_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > uchi_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ta_tokoro.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ta_totan.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_irai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wake_deha_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_chushin_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_hajime_hajime_to_suru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_nuki_ni_shite_ha_nuki_ni_shite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > yo_ni_n3.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ageku_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > amari_excessif.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > bakari_ka_bakari_de_naku.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_gatai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_gimi.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_kakeru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_kanenai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_kaneru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_kiru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_nuku.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_uru_enai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > dake_quantite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > dake_atte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > dake_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > dokoro_deha_nai_naku.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > conjecturale_deha_nai_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ge.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ha_tomokaku.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > hanmen.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > hodo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > hoka_nai_shikata_ga_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > igai_no.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ijo_ha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ippo_ippo_deha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > jo_ha_mo_no.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ka_to_omou_to_omottara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kagiri.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kagiri_deha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kanoyo_ni_na_da.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kara_iu_to_ieba_itte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kara_mite_mo_miru_to_mireba.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kara_ni_ha_ha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kara_suru_to_sureba_shite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kara_to_itte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > karakoso.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ki_ga_suru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kiri.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kke.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koto_da_recommandation.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koto_dakara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koto_ha_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koto_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koto_kara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koto_naku.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > koto_ni_ha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > kurai_gurai_hodo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mai_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mo_kamawazu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mono_da_n2.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mono_dakara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mono_ga_aru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mono_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mono_nara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mono_no.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > muke.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > muki.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nado_nanka_nante.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nai_koto_ha_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nai_koto_ni_ha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nakanaka_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_atatte_atari.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_chigai_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_hanshite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_hoka_naranai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kagirazu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kagiru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kakawarazu_kakawarinaku.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kakete_ha_mo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_kotaete.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_mo_kakawarazu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_moto_zuite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_oite_okeru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_sai_shite_sai_shi.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_sakidatte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_shiro_shitemo_seyo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_shitagatte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_shitara_sureba_shite_mo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_shite_ha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_sotte_soi_sou.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_suginai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_soi_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_tomonatte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_tsuite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_tsuki.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_tsurete.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_wataru_watatte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ni_ojite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > no_moto_de_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nomi_narazu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nuki_de_no.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > osore_ga_aru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ppoi.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > sae_karashite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > sai_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > saichu_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > shidai_deha_da.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > shikanai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > sue_ni_no_sue.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_itte_mo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_iu_koto_da.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_iu_mono_da.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_iu_mono_deha_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_iu_yori.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_shitara_sureba.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_shite_ha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_shite_mo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_tomo_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > toka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > tsutsu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ue_de.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ue_ha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ue_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > eba_verb_neutre_hodo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > naide_zuni_ha_irarenai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ta_kiri.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_hajimete.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_karade_nai_to_nakereba.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_naranai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_tamaranai_shoganai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > keigo_vocabulaire.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wake_desu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wake_ga_nai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wake_ni_ha_ikanai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wari_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_keiki_ni_shite_toshite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_kikkake_ni_shite_toshite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_komete.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_megutte.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_moto_ni_shite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_towazu_ha_towazu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > wo_tsujite_tooshite.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > zaru_wo_enai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ka_nai_ka_no_uchi_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > yara_yara.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > mo_eba_nara_mo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_naosu.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > base_owaru.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > conjecturale_to_omou.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > goro_gurai_yaku.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > hitsuyo.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ichio.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > nidoto.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > sonna_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > tada_no_tan_ni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > to_iu_no_ha.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > eba_ii_noni.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > ta_ra_ii_desu_ka.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > te_mo_shoganai_shikataganai.html
echo "<html>
	<head></head>
	<body>
	</body>
</html>" > zutsu.html
