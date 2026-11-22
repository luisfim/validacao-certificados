const $ = (id) => document.getElementById(id);
const nomesStatus = {APROVADO_NA_TRIAGEM:'Aprovado na triagem',PENDENTE:'Pendente de conferência',REJEITADO:'Rejeitado'};
const campos = {participante:'Participante',atividade:'Atividade',emissor:'Instituição',horas:'Horas',inicio:'Início',termino:'Término',emissao:'Emissão',codigo:'Código'};
let processando = false;
async function pedido(url, opcoes) {
    const r = await fetch(url, opcoes);
    const body = await r.json();
    if (!r.ok) throw new Error(body.erro || 'Não foi possível completar a solicitação.');
    return body;
}
async function exemplo(tipo) {
    const r = await fetch('exemplos/'+tipo+'.txt');
    if (!r.ok) throw new Error('Não foi possível carregar o exemplo.');
    $('texto').value = await r.text(); $('aluno').value = '1';
}
function mostrar(a) {
    $('vazio').hidden = true; $('resultado').hidden = false;
    $('status').textContent = nomesStatus[a.status] || a.status;
    $('status').className = 'status '+a.status;
    $('modo').textContent = a.modoExtracao+' · '+(a.origem==='ARQUIVO'?'Leitura por OCR':'Texto informado');
    $('motivos').replaceChildren();
    a.motivos.forEach(m=>{const li=document.createElement('li');li.textContent=m;$('motivos').append(li);});
    $('campos').replaceChildren();
    for (const [chave,nome] of Object.entries(campos)) {
        const dt=document.createElement('dt'), dd=document.createElement('dd');
        dt.textContent=nome; dd.textContent=a.dados[chave] ?? 'Não encontrado'; $('campos').append(dt,dd);
    }
    $('etapas').replaceChildren();
    a.etapas.forEach(e=>{const li=document.createElement('li');li.textContent=e;$('etapas').append(li);});
    $('textoExtraido').textContent=a.textoExtraido;
    $('json').href='/api/analises/'+encodeURIComponent(a.id);
}
async function historico() {
    try {
        $('historicoErro').textContent='';
        const itens=await pedido('/api/analises'); $('historico').replaceChildren();
        if (!itens.length) {
            const tr=document.createElement('tr'), td=document.createElement('td');
            td.colSpan=5;td.textContent='Nenhuma análise ainda.';tr.append(td);$('historico').append(tr);
        }
        for (const a of itens) {
            const tr=document.createElement('tr');
            for (const valor of [new Date(a.criadoEm).toLocaleString('pt-BR'),a.aluno.nome,a.dados.atividade||'Não identificada',nomesStatus[a.status]]) {
                const td=document.createElement('td');td.textContent=valor;tr.append(td);
            }
            const td=document.createElement('td'), b=document.createElement('button');b.textContent='Ver';b.type='button';
            b.onclick=()=>{mostrar(a);$('resultado').scrollIntoView({behavior:'smooth',block:'center'});};td.append(b);tr.append(td);$('historico').append(tr);
        }
    } catch(e) {$('historicoErro').textContent=e.message;}
}
document.querySelectorAll('[name=origem]').forEach(r=>r.onchange=()=>{
    const arquivo=document.querySelector('[name=origem]:checked').value==='arquivo';
    $('painelArquivo').hidden=!arquivo; $('painelTexto').hidden=arquivo;
});
document.querySelectorAll('[data-exemplo]').forEach(b=>b.onclick=()=>exemplo(b.dataset.exemplo).catch(e=>{ $('erro').hidden=false;$('erro').textContent=e.message; }));
$('atualizar').onclick=historico;
$('form').onsubmit=async(e)=>{
    e.preventDefault();if(processando)return;
    $('erro').hidden=true;processando=true;$('enviar').disabled=true;$('enviar').textContent='Processando...';
    try {
        const arquivo=document.querySelector('[name=origem]:checked').value==='arquivo';
        let a;
        if(arquivo) {
            const f=$('arquivo').files[0];if(!f)throw new Error('Selecione um arquivo.');
            if(f.size>5*1024*1024)throw new Error('O arquivo deve ter até 5 MB.');
            const form=new FormData();form.append('alunoId',$('aluno').value);form.append('usarIa',$('ia').checked);form.append('arquivo',f);
            a=await pedido('/api/analises/arquivo',{method:'POST',body:form});
        } else {
            if(!$('texto').value.trim())throw new Error('Informe o texto ou escolha um exemplo.');
            a=await pedido('/api/analises/texto',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({alunoId:$('aluno').value,texto:$('texto').value,usarIa:$('ia').checked})});
        }
        mostrar(a);await historico();
    } catch(e){$('erro').hidden=false;$('erro').textContent=e.message;}
    finally{processando=false;$('enviar').disabled=false;$('enviar').textContent='Analisar certificado';}
};
(async()=>{
    try {
        const [alunos,config]=await Promise.all([pedido('/api/alunos'),pedido('/api/config')]);
        $('aluno').replaceChildren();alunos.forEach(a=>{const o=document.createElement('option');o.value=a.id;o.textContent=a.nome+' (ingresso: '+a.ingresso+')';$('aluno').append(o);});
        $('ia').disabled=!config.iaConfigurada;
        $('iaStatus').textContent=config.iaConfigurada?'Chave configurada. Ao marcar, o texto será enviado à OpenAI.':'Sem chave configurada. A extração local funciona com os campos rotulados dos exemplos.';
        await exemplo('valido');await historico();
    } catch(e){$('erro').hidden=false;$('erro').textContent=e.message;}
})();
