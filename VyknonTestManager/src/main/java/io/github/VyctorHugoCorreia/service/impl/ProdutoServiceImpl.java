package io.github.VyctorHugoCorreia.service.impl;


import io.github.VyctorHugoCorreia.domain.entity.ProdutoEntity;
import io.github.VyctorHugoCorreia.domain.entity.TimeEntity;
import io.github.VyctorHugoCorreia.domain.repository.ProdutoRepository;
import io.github.VyctorHugoCorreia.domain.repository.TimeRepository;
import io.github.VyctorHugoCorreia.exception.ProdutoNaoEncontradoException;
import io.github.VyctorHugoCorreia.exception.RegraNegocioException;
import io.github.VyctorHugoCorreia.exception.TimeNaoEncontradoException;
import io.github.VyctorHugoCorreia.rest.dto.ProdutoDTO;
import io.github.VyctorHugoCorreia.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final TimeRepository timeRepository;


    @Override
    @Transactional
    public ProdutoEntity salvar(ProdutoDTO dto) {
        Long idTime = dto.getIdTime();
        TimeEntity time = timeRepository
                .findById(idTime.intValue())
                .orElseThrow(TimeNaoEncontradoException::new);

        String descProduto = dto.getDescProduto();
        validarProdutoParaTimeExistente(descProduto, time);

        ProdutoEntity produto = new ProdutoEntity();
        produto.setDescProduto(descProduto);
        produto.setIdTime(time);

        return produtoRepository.save(produto);
    }

    @Override
    public ProdutoEntity editar(Long id, ProdutoDTO dto) {
        ProdutoEntity existingProduto = getExistingProduto(id);
        String novoProdutoDesc = dto.getDescProduto();

        updateDescProduto(existingProduto, novoProdutoDesc);

        if (dto.getIdTime() != null && !dto.getIdTime().equals(existingProduto.getIdTime().getIdTime())) {
            Long novoIdTime = dto.getIdTime();
            TimeEntity novoTime = getTime(novoIdTime);
            validarProdutoParaNovoTime(novoProdutoDesc, novoTime);
            existingProduto.setIdTime(novoTime);
        }

        return produtoRepository.save(existingProduto);
    }

    @Override
    @Transactional
    public String deletar(Long id) {
        produtoRepository.delete(getExistingProduto(id));
        return "Produto deletado com sucesso.";
    }

    private ProdutoEntity getExistingProduto(Long id) {
        return produtoRepository.findById(id.intValue())
                .orElseThrow(ProdutoNaoEncontradoException::new);
    }

    private void updateDescProduto(ProdutoEntity produto, String novoProdutoDesc) {
        if (!novoProdutoDesc.equals(produto.getDescProduto())) {
            validarProdutoParaTimeExistente(novoProdutoDesc, produto.getIdTime());
            produto.setDescProduto(novoProdutoDesc);
        }
    }

    private TimeEntity getTime(Long idTime) {
        return timeRepository.findById(idTime.intValue())
                .orElseThrow(TimeNaoEncontradoException::new);
    }

    private void validarProdutoParaTimeExistente(String novoProdutoDesc, TimeEntity time) {
        if (produtoRepository.existsByDescProdutoAndIdTime(novoProdutoDesc, time)) {
            throw new RegraNegocioException("Já existe um produto com o mesmo nome para este time.");
        }
    }

    private void validarProdutoParaNovoTime(String novoProdutoDesc, TimeEntity novoTime) {
        validarProdutoParaTimeExistente(novoProdutoDesc, novoTime);
    }

}
