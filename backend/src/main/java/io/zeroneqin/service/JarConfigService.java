package io.zeroneqin.service;

import io.zeroneqin.api.jmeter.NewDriverManager;
import io.zeroneqin.base.domain.JarConfig;
import io.zeroneqin.base.domain.JarConfigExample;
import io.zeroneqin.base.mapper.JarConfigMapper;
import io.zeroneqin.commons.exception.MSException;
import io.zeroneqin.commons.utils.LogUtil;
import io.zeroneqin.commons.utils.SessionUtils;
import io.zeroneqin.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class JarConfigService {

    private static final String JAR_FILE_DIR = "/opt/zeroneqin/data/jar";

    @Resource
    private JarConfigMapper jarConfigMapper;

    public List<JarConfig> list() {
        JarConfigExample example = new JarConfigExample();
        return jarConfigMapper.selectByExample(example);
    }

    public List<JarConfig> list(JarConfig jarConfig) {
        JarConfigExample example = new JarConfigExample();
        if (StringUtils.isNotBlank(jarConfig.getName())) {
            example.createCriteria().andNameLike("%" + jarConfig.getName() + "%");
        }
        example.setOrderByClause("update_time desc");
        return jarConfigMapper.selectByExample(example);
    }

    public JarConfig get(String id) {
        return jarConfigMapper.selectByPrimaryKey(id);
    }

    public void delete(String id) {
        JarConfig JarConfig = jarConfigMapper.selectByPrimaryKey(id);
        deleteJarFile(JarConfig.getPath());
        jarConfigMapper.deleteByPrimaryKey(id);
    }

    public void update(JarConfig jarConfig, MultipartFile file) {
        checkExist(jarConfig);
        jarConfig.setEnable(true);// todo 审批机制时需修改
        jarConfig.setModifier(SessionUtils.getUser().getId());
        jarConfig.setUpdateTime(System.currentTimeMillis());
        String deletePath = jarConfig.getPath();
        if (file != null) {
            jarConfig.setFileName(file.getOriginalFilename());
            jarConfig.setPath(getJarPath(file));
        }
        jarConfigMapper.updateByPrimaryKey(jarConfig);
        if (file != null) {
            deleteJarFile(deletePath);
            createJarFiles(file);
            NewDriverManager.loadJar(jarConfig.getPath());
        }
    }

    public String add(JarConfig jarConfig, MultipartFile file) {
        jarConfig.setId(UUID.randomUUID().toString());
        jarConfig.setCreator(SessionUtils.getUser().getId());
        jarConfig.setModifier(SessionUtils.getUser().getId());
        checkExist(jarConfig);
        jarConfig.setEnable(true);// todo 审批机制时需修改
        jarConfig.setCreateTime(System.currentTimeMillis());
        jarConfig.setUpdateTime(System.currentTimeMillis());
        jarConfig.setPath(getJarPath(file));
        jarConfig.setFileName(file.getOriginalFilename());
        jarConfigMapper.insert(jarConfig);
        createJarFiles(file);
        NewDriverManager.loadJar(jarConfig.getPath());
        return jarConfig.getId();
    }

    public void deleteJarFiles(String testId) {
        File file = new File(JAR_FILE_DIR + "/" + testId);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
    }

    public void deleteJarFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public String getJarPath(MultipartFile file) {
        return JAR_FILE_DIR + "/" + file.getOriginalFilename();
    }

    private String createJarFiles(MultipartFile jar) {
        if (jar == null) {
            return null;
        }
        File testDir = new File(JAR_FILE_DIR);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        String filePath = testDir + "/" + jar.getOriginalFilename();
        File file = new File(filePath);
        try (InputStream in = jar.getInputStream(); OutputStream out = new FileOutputStream(file)) {
            file.createNewFile();
            FileUtil.copyStream(in, out);
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("upload_fail"));
        }
        return filePath;
    }

    private void checkExist(JarConfig jarConfig) {
        if (jarConfig.getName() != null) {
            JarConfigExample example = new JarConfigExample();
            JarConfigExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(jarConfig.getName());
            if (StringUtils.isNotBlank(jarConfig.getId())) {
                criteria.andIdNotEqualTo(jarConfig.getId());
            }
            if (jarConfigMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("already_exists"));
            }
        }
    }
}
